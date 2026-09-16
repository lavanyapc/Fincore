package com.smartbank.dto;

public class OtpResponse {
    private String otpCode;
    private String message;

    public OtpResponse(String otpCode, String message) {
        this.otpCode = otpCode;
        this.message = message;
    }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}