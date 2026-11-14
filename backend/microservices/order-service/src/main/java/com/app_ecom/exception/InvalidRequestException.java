package com.app_ecom.exception;

import org.springframework.http.HttpStatus;

/** The request is valid in form but cannot be processed in the current state (empty cart, wrong status...). */
public class InvalidRequestException extends BusinessException {
    public InvalidRequestException(String title, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, title, message);
    }
}
