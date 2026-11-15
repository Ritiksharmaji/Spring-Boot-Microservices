package com.ecommerce.shop.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Every error is returned as RFC 7807 application/problem+json. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail business(BusinessException ex) {
        return problem(ex.getStatus(), ex.getTitle(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation Failed", "Request has invalid fields");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail parameterValidation(HandlerMethodValidationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Validation Failed", "Request has invalid parameters");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed Request", "Request body is missing or not valid JSON");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail tooLarge(MaxUploadSizeExceededException ex) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Image Too Large", "Images can be at most 5 MB");
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    public ProblemDetail badMultipart(Exception ex) {
        return problem(HttpStatus.BAD_REQUEST, "Image Required", "Send the image as multipart form field 'file'");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail denied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", "You are not allowed to do this");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail noResource(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", "No such endpoint");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something went wrong");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
