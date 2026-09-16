package com.smartbank.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartbank.dto.OtpResponse;
import com.smartbank.entity.OtpVerification;
import com.smartbank.entity.User;
import com.smartbank.repository.OtpVerificationRepository;
import com.smartbank.repository.UserRepository;

@Service
public class OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpVerificationRepository otpVerificationRepository, UserRepository userRepository) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public OtpResponse generateOtp(String purpose) {
        User user = getCurrentUser();

        String code = String.format("%06d", random.nextInt(1_000_000));

        OtpVerification otp = new OtpVerification();
        otp.setUser(user);
        otp.setOtpCode(code);
        otp.setPurpose(purpose);
        otp.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        otp.setUsed(false);
        otpVerificationRepository.save(otp);

        return new OtpResponse(code, "OTP generated. Valid for 5 minutes. (Simulated - in production this would be sent via SMS/email, not returned here.)");
    }

    public boolean verifyOtp(String code, String purpose) {
        User user = getCurrentUser();

        return otpVerificationRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .filter(o -> o.getOtpCode().equals(code))
                .filter(o -> o.getPurpose().equals(purpose))
                .filter(o -> !o.isUsed())
                .filter(o -> o.getExpiresAt().isAfter(OffsetDateTime.now()))
                .findFirst()
                .map(o -> {
                    o.setUsed(true);
                    otpVerificationRepository.save(o);
                    return true;
                })
                .orElse(false);
    }
}