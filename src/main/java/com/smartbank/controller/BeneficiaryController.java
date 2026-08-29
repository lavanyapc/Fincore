package com.smartbank.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartbank.dto.AddBeneficiaryRequest;
import com.smartbank.dto.BeneficiaryResponse;
import com.smartbank.service.BeneficiaryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(@Valid @RequestBody AddBeneficiaryRequest request) {
        return ResponseEntity.ok(beneficiaryService.addBeneficiary(request));
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> getMyBeneficiaries() {
        return ResponseEntity.ok(beneficiaryService.getMyBeneficiaries());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(@PathVariable UUID id) {
        beneficiaryService.deleteBeneficiary(id);
        return ResponseEntity.noContent().build();
    }
}