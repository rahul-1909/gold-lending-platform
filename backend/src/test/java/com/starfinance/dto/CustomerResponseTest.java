package com.starfinance.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerResponseTest {

    @Test
    void testAadhaarMasking() {
        assertEquals("XXXX-XXXX-9012", CustomerResponse.maskAadhaar("123456789012"));
        assertNull(CustomerResponse.maskAadhaar(null));
        assertEquals("12", CustomerResponse.maskAadhaar("12"));
    }

    @Test
    void testPanMasking() {
        assertEquals("XXXXXX234F", CustomerResponse.maskPan("ABCDE1234F"));
        assertNull(CustomerResponse.maskPan(null));
    }

    @Test
    void testAccountNumberMasking() {
        assertEquals("XXXXXX7890", CustomerResponse.maskAccountNumber("1234567890"));
        assertNull(CustomerResponse.maskAccountNumber(null));
    }
}
