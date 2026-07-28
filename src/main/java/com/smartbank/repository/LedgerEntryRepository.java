// LedgerEntryRepository.java
package com.smartbank.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartbank.smartbank.entity.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
