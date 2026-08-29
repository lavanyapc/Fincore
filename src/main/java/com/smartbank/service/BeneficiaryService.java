package com.smartbank.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartbank.dto.AddBeneficiaryRequest;
import com.smartbank.dto.BeneficiaryResponse;
import com.smartbank.entity.Account;
import com.smartbank.entity.Beneficiary;
import com.smartbank.entity.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.BeneficiaryRepository;
import com.smartbank.repository.UserRepository;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository, AccountRepository accountRepository,
                               UserRepository userRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public BeneficiaryResponse addBeneficiary(AddBeneficiaryRequest request) {
        User currentUser = getCurrentUser();

        Account targetAccount = accountRepository.findByAccountNumber(request.getBeneficiaryAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("No account found with that account number"));

        if (targetAccount.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot add your own account as a beneficiary");
        }

        boolean alreadyExists = beneficiaryRepository.findByOwnerId(currentUser.getId()).stream()
                .anyMatch(b -> b.getBeneficiaryAccountNumber().equals(request.getBeneficiaryAccountNumber()));
        if (alreadyExists) {
            throw new IllegalArgumentException("This account is already saved as a beneficiary");
        }

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setOwner(currentUser);
        beneficiary.setBeneficiaryAccountNumber(request.getBeneficiaryAccountNumber());
        beneficiary.setNickname(request.getNickname());

        beneficiaryRepository.save(beneficiary);

        return toResponse(beneficiary);
    }

    public List<BeneficiaryResponse> getMyBeneficiaries() {
        User currentUser = getCurrentUser();
        return beneficiaryRepository.findByOwnerId(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteBeneficiary(UUID beneficiaryId) {
        User currentUser = getCurrentUser();
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));

        if (!beneficiary.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Beneficiary does not belong to the current user");
        }

        beneficiaryRepository.delete(beneficiary);
    }

    private BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getBeneficiaryAccountNumber(),
                beneficiary.getNickname(),
                beneficiary.getAddedAt()
        );
    }
}