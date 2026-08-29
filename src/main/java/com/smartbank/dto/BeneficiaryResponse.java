package com.smartbank.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BeneficiaryResponse {

    private UUID id;
    private String beneficiaryAccountNumber;
    private String nickname;
    private OffsetDateTime addedAt;

    public BeneficiaryResponse(UUID id, String beneficiaryAccountNumber, String nickname, OffsetDateTime addedAt) {
        this.id = id;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.nickname = nickname;
        this.addedAt = addedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) { this.beneficiaryAccountNumber = beneficiaryAccountNumber; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public OffsetDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(OffsetDateTime addedAt) { this.addedAt = addedAt; }
}