package com.smartbank.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartbank.dto.CreateScheduledPaymentRequest;
import com.smartbank.dto.ScheduledPaymentResponse;
import com.smartbank.entity.Account;
import com.smartbank.entity.ScheduledPayment;
import com.smartbank.entity.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.ScheduledPaymentRepository;
import com.smartbank.repository.UserRepository;

@Service
public class ScheduledPaymentService {

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public ScheduledPaymentService(ScheduledPaymentRepository scheduledPaymentRepository,
                                    AccountRepository accountRepository, UserRepository userRepository) {
        this.scheduledPaymentRepository = scheduledPaymentRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public ScheduledPaymentResponse createScheduledPayment(CreateScheduledPaymentRequest request) {
        User currentUser = getCurrentUser();

        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        if (!sourceAccount.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Account does not belong to the current user");
        }

        if (!accountRepository.findByAccountNumber(request.getDestinationAccountNumber()).isPresent()) {
            throw new IllegalArgumentException("Destination account not found");
        }

        ScheduledPayment payment = new ScheduledPayment();
        payment.setSourceAccount(sourceAccount);
        payment.setDestinationAccountNumber(request.getDestinationAccountNumber());
        payment.setAmount(request.getAmount());
        payment.setFrequency(request.getFrequency());
        payment.setNextRunAt(request.getFirstRunAt());
        payment.setStatus("ACTIVE");

        scheduledPaymentRepository.save(payment);

        return toResponse(payment);
    }

    public List<ScheduledPaymentResponse> getMyScheduledPayments() {
        User currentUser = getCurrentUser();
        return scheduledPaymentRepository.findBySourceAccountUserId(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void cancelScheduledPayment(UUID paymentId) {
        User currentUser = getCurrentUser();
        ScheduledPayment payment = scheduledPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled payment not found"));

        if (!payment.getSourceAccount().getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Scheduled payment does not belong to the current user");
        }

        payment.setStatus("CANCELLED");
        scheduledPaymentRepository.save(payment);
    }

    private ScheduledPaymentResponse toResponse(ScheduledPayment payment) {
        return new ScheduledPaymentResponse(
                payment.getId(),
                payment.getDestinationAccountNumber(),
                payment.getAmount(),
                payment.getFrequency(),
                payment.getNextRunAt(),
                payment.getStatus()
        );
    }
}