package com.app_ecom.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Expected business-rule failure (bad input, missing resource, not enough stock...).
 * Resilience4j is configured to ignore these: they never trip a circuit breaker or trigger a retry.
 */
@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String title;

    public BusinessException(HttpStatus status, String title, String message) {
        super(message);
        this.status = status;
        this.title = title;
    }
}
