package com.progressoft.fxdeals.util;

import com.progressoft.fxdeals.dto.DealDTO;
import com.progressoft.fxdeals.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtil Tests")
class ValidationUtilTest {

    @Test
    @DisplayName("Should pass validation for valid deal")
    void shouldPassValidationForValidDeal() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        assertDoesNotThrow(() -> ValidationUtil.validateDeal(dto));
        assertEquals("USD", dto.getFromCurrency());
        assertEquals("EUR", dto.getToCurrency());
        assertNotNull(dto.getParsedTimestamp());
        assertNotNull(dto.getParsedAmount());
    }

    @Test
    @DisplayName("Should fail when dealId is null")
    void shouldFailWhenDealIdIsNull() {
        DealDTO dto = DealDTO.builder()
                .dealId(null)
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertEquals("Deal ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when dealId is empty")
    void shouldFailWhenDealIdIsEmpty() {
        DealDTO dto = DealDTO.builder()
                .dealId("")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertEquals("Deal ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when dealId is blank")
    void shouldFailWhenDealIdIsBlank() {
        DealDTO dto = DealDTO.builder()
                .dealId("   ")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertEquals("Deal ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when fromCurrency is missing")
    void shouldFailWhenFromCurrencyIsMissing() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency(null)
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertEquals("From Currency is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when toCurrency is missing")
    void shouldFailWhenToCurrencyIsMissing() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertEquals("To Currency is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when dealTimestamp is missing")
    void shouldFailWhenDealTimestampIsMissing() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp(null)
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertEquals("Deal Timestamp is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when dealAmount is missing")
    void shouldFailWhenDealAmountIsMissing() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount(null)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertEquals("Deal Amount is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail for invalid currency code XXX")
    void shouldFailForInvalidCurrencyCodeXXX() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("XXX")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertTrue(exception.getMessage().contains("Invalid From Currency code"));
        assertTrue(exception.getMessage().contains("XXX"));
    }

    @Test
    @DisplayName("Should fail for invalid currency format")
    void shouldFailForInvalidCurrencyFormat() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("US")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertTrue(exception.getMessage().contains("Invalid From Currency format"));
    }

    @Test
    @DisplayName("Should fail for non-existent currency code")
    void shouldFailForNonExistentCurrencyCode() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("ZZZ")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertTrue(exception.getMessage().contains("not a valid ISO 4217 currency"));
    }

    @Test
    @DisplayName("Should normalize currency codes to uppercase")
    void shouldNormalizeCurrencyCodesToUppercase() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("usd")
                .toCurrency("eur")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        assertDoesNotThrow(() -> ValidationUtil.validateDeal(dto));
        assertEquals("USD", dto.getFromCurrency());
        assertEquals("EUR", dto.getToCurrency());
    }

    @Test
    @DisplayName("Should fail for invalid timestamp format")
    void shouldFailForInvalidTimestampFormat() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("invalid-date")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertTrue(exception.getMessage().contains("Invalid timestamp format"));
        assertTrue(exception.getMessage().contains("invalid-date"));
    }

    @Test
    @DisplayName("Should fail for wrong timestamp format")
    void shouldFailForWrongTimestampFormat() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15 10:30:00")
                .dealAmount("1000.50")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertTrue(exception.getMessage().contains("Invalid timestamp format"));
    }

    @Test
    @DisplayName("Should fail for negative amount")
    void shouldFailForNegativeAmount() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("-500.00")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertTrue(exception.getMessage().contains("Deal amount must be positive"));
        assertTrue(exception.getMessage().contains("-500.00"));
    }

    @Test
    @DisplayName("Should fail for zero amount")
    void shouldFailForZeroAmount() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("0")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertTrue(exception.getMessage().contains("Deal amount must be positive"));
    }

    @Test
    @DisplayName("Should fail for non-numeric amount")
    void shouldFailForNonNumericAmount() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("not-a-number")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDeal(dto));
        assertTrue(exception.getMessage().contains("Invalid amount format"));
        assertTrue(exception.getMessage().contains("not-a-number"));
    }

    @Test
    @DisplayName("Should accept valid decimal amounts")
    void shouldAcceptValidDecimalAmounts() {
        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1234567.8901")
                .build();

        assertDoesNotThrow(() -> ValidationUtil.validateDeal(dto));
        assertNotNull(dto.getParsedAmount());
        assertEquals("1234567.8901", dto.getParsedAmount().toString());
    }
}