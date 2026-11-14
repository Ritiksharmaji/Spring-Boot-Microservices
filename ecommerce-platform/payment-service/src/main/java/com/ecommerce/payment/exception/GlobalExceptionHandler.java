package com.ecommerce.payment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Returns RFC 7807 problem+json responses for every error. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Missing authenticated user context");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something went wrong");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
