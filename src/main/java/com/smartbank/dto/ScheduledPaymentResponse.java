package com.smartbank.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ScheduledPaymentResponse {

    private UUID id;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String frequency;
    private OffsetDateTime nextRunAt;
    private String status;

    public ScheduledPaymentResponse(UUID id, String destinationAccountNumber, BigDecimal amount,
                                     String frequency, OffsetDateTime nextRunAt, String status) {
        this.id = id;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.frequency = frequency;
        this.nextRunAt = nextRunAt;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public OffsetDateTime getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(OffsetDateTime nextRunAt) { this.nextRunAt = nextRunAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}