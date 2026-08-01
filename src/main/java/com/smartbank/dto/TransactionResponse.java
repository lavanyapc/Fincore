package com.smartbank.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TransactionResponse {

    private UUID transactionId;
    private String type;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String status;
    private OffsetDateTime completedAt;

    public TransactionResponse(UUID transactionId, String type, BigDecimal amount,
                                BigDecimal newBalance, String status, OffsetDateTime completedAt) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.newBalance = newBalance;
        this.status = status;
        this.completedAt = completedAt;
    }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}