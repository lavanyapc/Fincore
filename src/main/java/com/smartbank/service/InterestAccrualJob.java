package com.smartbank.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartbank.entity.Account;
import com.smartbank.entity.LedgerEntry;
import com.smartbank.entity.Transaction;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.LedgerEntryRepository;
import com.smartbank.repository.TransactionRepository;

@Component
public class InterestAccrualJob {

    private static final Logger log = LoggerFactory.getLogger(InterestAccrualJob.class);
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public InterestAccrualJob(AccountRepository accountRepository, TransactionRepository transactionRepository,
                               LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    // Runs once every 24 hours. For demoing/testing, this interval can be shortened temporarily.
    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void accrueDailyInterest() {
        List<Account> savingsAccounts = accountRepository.findAll().stream()
                .filter(a -> "SAVINGS".equals(a.getAccountType()) && "ACTIVE".equals(a.getStatus()))
                .toList();

        for (Account account : savingsAccounts) {
            try {
                accrueInterestFor(account);
            } catch (Exception e) {
                log.error("Failed to accrue interest for account {}: {}", account.getId(), e.getMessage());
            }
        }
    }

    private void accrueInterestFor(Account account) {
        // Simple daily interest: balance * annual rate / 365, using day-count convention actual/365.
        BigDecimal dailyInterest = account.getBalance()
                .multiply(account.getInterestRate())
                .divide(DAYS_IN_YEAR, 4, RoundingMode.HALF_UP);

        if (dailyInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey("interest-" + account.getId() + "-" + java.time.LocalDate.now());
        transaction.setType("DEPOSIT");
        transaction.setDestinationAccount(account);
        transaction.setAmount(dailyInterest);
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        BigDecimal newBalance = account.getBalance().add(dailyInterest);
        account.setBalance(newBalance);
        accountRepository.save(account);

        LedgerEntry entry = new LedgerEntry();
        entry.setAccount(account);
        entry.setTransaction(transaction);
        entry.setEntryType("CREDIT");
        entry.setAmount(dailyInterest);
        entry.setBalanceAfter(newBalance);
        entry.setDescription("Daily interest accrual");
        ledgerEntryRepository.save(entry);

        log.info("Accrued {} interest for account {}", dailyInterest, account.getAccountNumber());
    }
}