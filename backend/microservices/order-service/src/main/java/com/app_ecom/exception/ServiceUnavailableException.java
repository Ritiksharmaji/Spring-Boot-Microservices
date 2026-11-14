package com.app_ecom.exception;

/** A downstream service could not be reached (after retries / with the circuit open). */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
