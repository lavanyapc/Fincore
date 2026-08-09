package com.smartbank.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartbank.entity.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    Page<LedgerEntry> findByAccountId(UUID accountId, Pageable pageable);

    List<LedgerEntry> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            UUID accountId, OffsetDateTime from, OffsetDateTime to);
}