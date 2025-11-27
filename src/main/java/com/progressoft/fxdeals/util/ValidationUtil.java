package com.progressoft.fxdeals.util;

import com.progressoft.fxdeals.dto.DealDTO;
import com.progressoft.fxdeals.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Currency;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class ValidationUtil {

    private static final Pattern ISO_CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final DateTimeFormatter ISO_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Blacklisted currency codes that are technically valid ISO 4217 but not real currencies
    private static final Set<String> BLACKLISTED_CURRENCIES = Set.of(
            "XXX", // No currency
            "XTS", // Test currency
            "XAU", // Gold (not a circulating currency)
            "XAG", // Silver
            "XPT", // Platinum
            "XPD"  // Palladium
    );

    private ValidationUtil() {}

    public static void validateDeal(DealDTO dealDTO) throws ValidationException {
        validateMandatoryFields(dealDTO);
        validateCurrencyCodes(dealDTO);
        validateAndParseTimestamp(dealDTO);
        validateAndParseAmount(dealDTO);
    }

    private static void validateMandatoryFields(DealDTO dealDTO) throws ValidationException {
        if (isBlank(dealDTO.getDealId())) throw new ValidationException("Deal ID is required");
        if (isBlank(dealDTO.getFromCurrency())) throw new ValidationException("From Currency is required");
        if (isBlank(dealDTO.getToCurrency())) throw new ValidationException("To Currency is required");
        if (isBlank(dealDTO.getDealTimestamp())) throw new ValidationException("Deal Timestamp is required");
        if (isBlank(dealDTO.getDealAmount())) throw new ValidationException("Deal Amount is required");
    }

    private static void validateCurrencyCodes(DealDTO dealDTO) throws ValidationException {
        String fromCurrency = dealDTO.getFromCurrency().trim().toUpperCase();
        String toCurrency = dealDTO.getToCurrency().trim().toUpperCase();

        if (!ISO_CURRENCY_PATTERN.matcher(fromCurrency).matches()) {
            throw new ValidationException(
                    String.format("Invalid From Currency format: '%s'. Must be 3 uppercase letters.", fromCurrency));
        }

        if (!ISO_CURRENCY_PATTERN.matcher(toCurrency).matches()) {
            throw new ValidationException(
                    String.format("Invalid To Currency format: '%s'. Must be 3 uppercase letters.", toCurrency));
        }

        // Check if currency is blacklisted
        if (BLACKLISTED_CURRENCIES.contains(fromCurrency)) {
            throw new ValidationException(
                    String.format("Invalid From Currency code: '%s' is not a valid ISO 4217 currency.", fromCurrency));
        }

        if (BLACKLISTED_CURRENCIES.contains(toCurrency)) {
            throw new ValidationException(
                    String.format("Invalid To Currency code: '%s' is not a valid ISO 4217 currency.", toCurrency));
        }

        try {
            Currency.getInstance(fromCurrency);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    String.format("Invalid From Currency code: '%s' is not a valid ISO 4217 currency.", fromCurrency));
        }

        try {
            Currency.getInstance(toCurrency);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    String.format("Invalid To Currency code: '%s' is not a valid ISO 4217 currency.", toCurrency));
        }

        dealDTO.setFromCurrency(fromCurrency);
        dealDTO.setToCurrency(toCurrency);
    }

    private static void validateAndParseTimestamp(DealDTO dealDTO) throws ValidationException {
        try {
            LocalDateTime ts = LocalDateTime.parse(dealDTO.getDealTimestamp().trim(), ISO_TIMESTAMP_FORMATTER);
            dealDTO.setParsedTimestamp(ts);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    String.format("Invalid timestamp format: '%s'. Expected format: yyyy-MM-dd'T'HH:mm:ss",
                            dealDTO.getDealTimestamp()), e);
        }
    }

    private static void validateAndParseAmount(DealDTO dealDTO) throws ValidationException {
        BigDecimal amount;
        try {
            amount = new BigDecimal(dealDTO.getDealAmount().trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(
                    String.format("Invalid amount format: '%s'. Must be a valid decimal number.",
                            dealDTO.getDealAmount()), e);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    String.format("Deal amount must be positive: '%s'", dealDTO.getDealAmount()));
        }

        dealDTO.setParsedAmount(amount);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}