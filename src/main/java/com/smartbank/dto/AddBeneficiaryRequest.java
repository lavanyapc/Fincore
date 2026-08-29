package com.smartbank.dto;

import jakarta.validation.constraints.NotBlank;

public class AddBeneficiaryRequest {

    @NotBlank(message = "Beneficiary account number is required")
    private String beneficiaryAccountNumber;

    private String nickname;

    public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) { this.beneficiaryAccountNumber = beneficiaryAccountNumber; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}