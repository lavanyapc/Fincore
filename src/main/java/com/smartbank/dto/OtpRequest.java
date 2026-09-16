package com.smartbank.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpRequest {
    @NotBlank(message = "Purpose is required")
    private String purpose;

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}