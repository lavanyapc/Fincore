package com.smartbank.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartbank.dto.CreateScheduledPaymentRequest;
import com.smartbank.dto.ScheduledPaymentResponse;
import com.smartbank.service.ScheduledPaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/scheduled-payments")
public class ScheduledPaymentController {

    private final ScheduledPaymentService scheduledPaymentService;

    public ScheduledPaymentController(ScheduledPaymentService scheduledPaymentService) {
        this.scheduledPaymentService = scheduledPaymentService;
    }

    @PostMapping
    public ResponseEntity<ScheduledPaymentResponse> create(@Valid @RequestBody CreateScheduledPaymentRequest request) {
        return ResponseEntity.ok(scheduledPaymentService.createScheduledPayment(request));
    }

    @GetMapping
    public ResponseEntity<List<ScheduledPaymentResponse>> getMyScheduledPayments() {
        return ResponseEntity.ok(scheduledPaymentService.getMyScheduledPayments());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        scheduledPaymentService.cancelScheduledPayment(id);
        return ResponseEntity.noContent().build();
    }
}