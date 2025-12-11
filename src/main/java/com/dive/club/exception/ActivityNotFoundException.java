package com.dive.club.exception;

/**
 * Exception thrown when activity is not found
 */
public class ActivityNotFoundException extends RuntimeException {

    public ActivityNotFoundException(String message) {
        super(message);
    }

    public ActivityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
