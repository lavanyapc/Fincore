package com.smartbank.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartbank.dto.DepositRequest;
import com.smartbank.dto.TransactionResponse;
import com.smartbank.dto.TransferRequest;
import com.smartbank.dto.WithdrawRequest;
import com.smartbank.entity.Account;
import com.smartbank.entity.LedgerEntry;
import com.smartbank.entity.Transaction;
import com.smartbank.entity.User;
import com.smartbank.exception.InsufficientFundsException;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.LedgerEntryRepository;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.repository.UserRepository;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public TransactionService(AccountRepository accountRepository, UserRepository userRepository,
                               TransactionRepository transactionRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    private Account getOwnedAccount(UUID accountId) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Account does not belong to the current user");
        }
        return account;
    }

    private Account getOwnedAccountForUpdate(UUID accountId) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Account does not belong to the current user");
        }
        return account;
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        Account account = getOwnedAccountForUpdate(request.getAccountId());

        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(UUID.randomUUID().toString());
        transaction.setType("DEPOSIT");
        transaction.setDestinationAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        LedgerEntry entry = new LedgerEntry();
        entry.setAccount(account);
        entry.setTransaction(transaction);
        entry.setEntryType("CREDIT");
        entry.setAmount(request.getAmount());
        entry.setBalanceAfter(newBalance);
        entry.setDescription("Deposit");
        ledgerEntryRepository.save(entry);

        return new TransactionResponse(transaction.getId(), "DEPOSIT", request.getAmount(),
                newBalance, "SUCCESS", transaction.getInitiatedAt());
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request) {
        Account account = getOwnedAccountForUpdate(request.getAccountId());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for this withdrawal");
        }

        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(UUID.randomUUID().toString());
        transaction.setType("WITHDRAW");
        transaction.setSourceAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        LedgerEntry entry = new LedgerEntry();
        entry.setAccount(account);
        entry.setTransaction(transaction);
        entry.setEntryType("DEBIT");
        entry.setAmount(request.getAmount());
        entry.setBalanceAfter(newBalance);
        entry.setDescription("Withdrawal");
        ledgerEntryRepository.save(entry);

        return new TransactionResponse(transaction.getId(), "WITHDRAW", request.getAmount(),
                newBalance, "SUCCESS", transaction.getInitiatedAt());
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, String idempotencyKey) {

        // Step 1: Check if we've already processed this exact request before.
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Transaction previous = existing.get();
            Account sourceAccount = previous.getSourceAccount();
            return new TransactionResponse(previous.getId(), previous.getType(), previous.getAmount(),
                    sourceAccount.getBalance(), previous.getStatus(), previous.getCompletedAt());
        }

        // Step 2: Not a duplicate — proceed with the normal transfer logic.
        Account sourceLookup = getOwnedAccount(request.getSourceAccountId());
        Account destinationLookup = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (sourceLookup.getId().equals(destinationLookup.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        UUID firstId, secondId;
        if (sourceLookup.getId().compareTo(destinationLookup.getId()) < 0) {
            firstId = sourceLookup.getId();
            secondId = destinationLookup.getId();
        } else {
            firstId = destinationLookup.getId();
            secondId = sourceLookup.getId();
        }

        Account firstLocked = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        Account secondLocked = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        Account sourceAccount = firstLocked.getId().equals(sourceLookup.getId()) ? firstLocked : secondLocked;
        Account destinationAccount = firstLocked.getId().equals(destinationLookup.getId()) ? firstLocked : secondLocked;

        if (!"ACTIVE".equals(sourceAccount.getStatus())) {
            throw new IllegalArgumentException("Source account is not active");
        }
        if (!"ACTIVE".equals(destinationAccount.getStatus())) {
            throw new IllegalArgumentException("Destination account is not active");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for this transfer");
        }

        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setType("TRANSFER");
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        BigDecimal sourceNewBalance = sourceAccount.getBalance().subtract(request.getAmount());
        sourceAccount.setBalance(sourceNewBalance);
        accountRepository.save(sourceAccount);

        BigDecimal destinationNewBalance = destinationAccount.getBalance().add(request.getAmount());
        destinationAccount.setBalance(destinationNewBalance);
        accountRepository.save(destinationAccount);

        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setAccount(sourceAccount);
        debitEntry.setTransaction(transaction);
        debitEntry.setEntryType("DEBIT");
        debitEntry.setAmount(request.getAmount());
        debitEntry.setBalanceAfter(sourceNewBalance);
        debitEntry.setDescription("Transfer to " + destinationAccount.getAccountNumber());
        ledgerEntryRepository.save(debitEntry);

        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setAccount(destinationAccount);
        creditEntry.setTransaction(transaction);
        creditEntry.setEntryType("CREDIT");
        creditEntry.setAmount(request.getAmount());
        creditEntry.setBalanceAfter(destinationNewBalance);
        creditEntry.setDescription("Transfer from " + sourceAccount.getAccountNumber());
        ledgerEntryRepository.save(creditEntry);

        return new TransactionResponse(transaction.getId(), "TRANSFER", request.getAmount(),
                sourceNewBalance, "SUCCESS", transaction.getInitiatedAt());
    }
}