package com.smartbank.controller;

import com.smartbank.service.AccountAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/accounts")
public class AccountAdminController {

    private final AccountAdminService accountAdminService;

    public AccountAdminController(AccountAdminService accountAdminService) {
        this.accountAdminService = accountAdminService;
    }

    @PatchMapping("/{id}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> freeze(@PathVariable UUID id) {
        accountAdminService.setAccountStatus(id, "FROZEN");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unfreeze(@PathVariable UUID id) {
        accountAdminService.setAccountStatus(id, "ACTIVE");
        return ResponseEntity.noContent().build();
    }
}