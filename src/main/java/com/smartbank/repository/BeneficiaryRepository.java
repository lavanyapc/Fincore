// BeneficiaryRepository.java
package com.smartbank.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartbank.smartbank.entity.Beneficiary;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    List<Beneficiary> findByOwnerId(UUID ownerId);
}