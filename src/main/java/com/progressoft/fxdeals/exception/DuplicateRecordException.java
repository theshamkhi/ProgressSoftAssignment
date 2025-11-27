package com.progressoft.fxdeals.exception;

/**
 * Exception thrown when attempting to insert a duplicate deal record.
 */
public class DuplicateRecordException extends RuntimeException {

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}