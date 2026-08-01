// AuditLogRepository.java
package com.smartbank.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartbank.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
