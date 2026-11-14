package com.app_ecom.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(String message) {
        super(HttpStatus.CONFLICT, "Insufficient Stock", message);
    }
}
