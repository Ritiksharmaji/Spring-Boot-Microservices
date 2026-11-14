package com.app_ecom.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "Not Found", message);
    }
}
