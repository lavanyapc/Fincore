package com.smartbank.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountResponse {

    private UUID id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String status;

    public AccountResponse(UUID id, String accountNumber, String accountType, BigDecimal balance, String status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}