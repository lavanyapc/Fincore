package com.smartbank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartbank.dto.OtpRequest;
import com.smartbank.dto.OtpResponse;
import com.smartbank.service.OtpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/generate")
    public ResponseEntity<OtpResponse> generate(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(otpService.generateOtp(request.getPurpose()));
    }
}
