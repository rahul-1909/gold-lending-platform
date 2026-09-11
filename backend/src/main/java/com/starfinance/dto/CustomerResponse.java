package com.starfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private String knNumber;
    private String aadhaar;
    private String panCard;
    private String bankAccountNumber;
    private String maskedAadhaar;
    private String maskedPanCard;
    private Boolean kycStatus;
    private Boolean kycVerified;
    private String maskedBankAccountNumber;
    private String city;
    private String state;
    private String pinCode;
    private String fullAddress;
    private String dateOfBirth;
    private String gender;
    private String mobileNumber;
    private String occupation;
    private String income;
    private String ifscCode;
    private String existingLoans;
    private String passportNumber;
    private String role;

    public static String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) return aadhaar;
        return "XXXX-XXXX-" + aadhaar.substring(aadhaar.length() - 4);
    }

    public static String maskPan(String pan) {
        if (pan == null || pan.length() < 4) return pan;
        return "XXXXXX" + pan.substring(pan.length() - 4);
    }

    public static String maskAccountNumber(String acc) {
        if (acc == null || acc.length() < 4) return acc;
        return "XXXXXX" + acc.substring(acc.length() - 4);
    }
}
