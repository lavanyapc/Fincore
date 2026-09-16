package com.smartbank.service;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartbank.entity.Account;
import com.smartbank.entity.AuditLog;
import com.smartbank.entity.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.AuditLogRepository;
import com.smartbank.repository.UserRepository;

@Service
public class AccountAdminService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public AccountAdminService(AccountRepository accountRepository, UserRepository userRepository,
                                AuditLogRepository auditLogRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public void setAccountStatus(UUID accountId, String newStatus) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        String oldStatus = account.getStatus();
        account.setStatus(newStatus);
        accountRepository.save(account);

        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(adminEmail).orElse(null);

        AuditLog log = new AuditLog();
        log.setActor(admin);
        log.setAction("ACCOUNT_STATUS_CHANGED");
        log.setEntityType("Account");
        log.setEntityId(account.getId());
        log.setDetails("{\"oldStatus\":\"" + oldStatus + "\",\"newStatus\":\"" + newStatus + "\"}");
        auditLogRepository.save(log);
    }
}