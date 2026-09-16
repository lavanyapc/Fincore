package com.smartbank.service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartbank.dto.TransferRequest;
import com.smartbank.entity.ScheduledPayment;
import com.smartbank.repository.ScheduledPaymentRepository;

@Component
public class ScheduledPaymentJob {

    private static final Logger log = LoggerFactory.getLogger(ScheduledPaymentJob.class);

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final TransactionService transactionService;

    public ScheduledPaymentJob(ScheduledPaymentRepository scheduledPaymentRepository, TransactionService transactionService) {
        this.scheduledPaymentRepository = scheduledPaymentRepository;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processDuePayments() {
        List<ScheduledPayment> duePayments = scheduledPaymentRepository
                .findByStatusAndNextRunAtBefore("ACTIVE", OffsetDateTime.now());

        for (ScheduledPayment payment : duePayments) {
            try {
                runPayment(payment);
            } catch (Exception e) {
                log.error("Failed to process scheduled payment {}: {}", payment.getId(), e.getMessage());
            }
        }
    }

    private void runPayment(ScheduledPayment payment) {
        String ownerEmail = payment.getSourceAccount().getUser().getEmail();
        var authentication = new UsernamePasswordAuthenticationToken(ownerEmail, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountId(payment.getSourceAccount().getId());
        transferRequest.setDestinationAccountNumber(payment.getDestinationAccountNumber());
        transferRequest.setAmount(payment.getAmount());

        String idempotencyKey = "scheduled-" + payment.getId() + "-" + payment.getNextRunAt();
        transactionService.transfer(transferRequest, idempotencyKey);

        log.info("Processed scheduled payment {} for account {}", payment.getId(), payment.getSourceAccount().getAccountNumber());

        updateNextRun(payment);

        SecurityContextHolder.clearContext();
    }

    private void updateNextRun(ScheduledPayment payment) {
        switch (payment.getFrequency()) {
            case "ONCE" -> payment.setStatus("COMPLETED");
            case "WEEKLY" -> payment.setNextRunAt(payment.getNextRunAt().plusWeeks(1));
            case "MONTHLY" -> payment.setNextRunAt(payment.getNextRunAt().plusMonths(1));
            default -> payment.setStatus("CANCELLED");
        }
        scheduledPaymentRepository.save(payment);
    }
}