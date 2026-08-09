package com.smartbank.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartbank.dto.AccountResponse;
import com.smartbank.dto.CreateAccountRequest;
import com.smartbank.dto.TransactionHistoryResponse;
import com.smartbank.entity.Account;
import com.smartbank.entity.LedgerEntry;
import com.smartbank.entity.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.LedgerEntryRepository;
import com.smartbank.repository.UserRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final LedgerEntryRepository ledgerEntryRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository,
                           AccountNumberGenerator accountNumberGenerator, LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    private Account getOwnedAccountReadOnly(UUID accountId) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Account does not belong to the current user");
        }
        return account;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        User currentUser = getCurrentUser();

        Account account = new Account();
        account.setUser(currentUser);
        account.setAccountNumber(accountNumberGenerator.generate());
        account.setAccountType(request.getAccountType());

        accountRepository.save(account);

        return toResponse(account);
    }

    public List<AccountResponse> getMyAccounts() {
        User currentUser = getCurrentUser();
        return accountRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<TransactionHistoryResponse> getTransactionHistory(UUID accountId, int page, int size) {
        Account account = getOwnedAccountReadOnly(accountId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ledgerEntryRepository.findByAccountId(account.getId(), pageable)
                .map(this::toHistoryResponse);
    }

    public List<TransactionHistoryResponse> getStatement(UUID accountId, OffsetDateTime from, OffsetDateTime to) {
        Account account = getOwnedAccountReadOnly(accountId);
        return ledgerEntryRepository.findByAccountIdAndCreatedAtBetweenOrderByCreatedAtAsc(account.getId(), from, to)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus()
        );
    }

    private TransactionHistoryResponse toHistoryResponse(LedgerEntry entry) {
        return new TransactionHistoryResponse(
                entry.getId(), entry.getEntryType(), entry.getAmount(),
                entry.getBalanceAfter(), entry.getDescription(), entry.getCreatedAt());
    }
}