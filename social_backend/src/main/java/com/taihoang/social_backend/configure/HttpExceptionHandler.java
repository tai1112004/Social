package com.taihoang.social_backend.configure;

import com.taihoang.social_backend.dto.ApiErrorResponse;
import com.taihoang.social_backend.exception.PostAccessDeniedException;
import com.taihoang.social_backend.exception.PostNotFoundException;
import com.taihoang.social_backend.exception.NotificationNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class HttpExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpExceptionHandler.class);

    @ExceptionHandler(PostNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(PostNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notificationNotFound(NotificationNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PostAccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> forbidden(PostAccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiErrorResponse> status(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        return error(status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status, ex.getReason(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? error.getField() + " khong hop le" : error.getDefaultMessage())
                .distinct().collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message.isBlank() ? "Du lieu gui len khong hop le" : message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> unreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Body JSON khong hop le (kiem tra privacy va kieu du lieu)", request);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiErrorResponse> invalidQuery(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Tham so truy van khong hop le", request);
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ResponseEntity<ApiErrorResponse> badRequest(RuntimeException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Database constraint rejected request {}", request.getRequestURI(), ex);
        return error(HttpStatus.CONFLICT, "Du lieu chia se khong hop le hoac da ton tai", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled HTTP error at {}", request.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Co loi xay ra khi xu ly yeu cau", request);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(),
                message == null || message.isBlank() ? status.getReasonPhrase() : message,
                request.getRequestURI()));
    }
}
