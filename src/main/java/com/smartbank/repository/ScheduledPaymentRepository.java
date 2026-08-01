// ScheduledPaymentRepository.java
package com.smartbank.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartbank.entity.ScheduledPayment;

public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, UUID> {
    List<ScheduledPayment> findByStatusAndNextRunAtBefore(String status, OffsetDateTime time);
}
