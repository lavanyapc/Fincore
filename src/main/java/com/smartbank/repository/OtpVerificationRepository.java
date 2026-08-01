// OtpVerificationRepository.java
package com.smartbank.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartbank.entity.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
}