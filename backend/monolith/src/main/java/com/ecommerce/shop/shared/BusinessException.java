package com.ecommerce.shop.shared;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Expected business-rule failure, rendered as RFC 7807 problem details. */
@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String title;

    public BusinessException(HttpStatus status, String title, String message) {
        super(message);
        this.status = status;
        this.title = title;
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, "Not Found", message);
    }

    public static BusinessException conflict(String title, String message) {
        return new BusinessException(HttpStatus.CONFLICT, title, message);
    }

    public static BusinessException invalid(String title, String message) {
        return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, title, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized", message);
    }

    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", message);
    }
}
