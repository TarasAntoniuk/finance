package com.tarasantoniuk.finance.common;

import com.tarasantoniuk.finance.banking.common.exeption.InsufficientBalanceException;
import com.tarasantoniuk.finance.common.document.exception.InvalidDocumentStatusException;
import com.tarasantoniuk.finance.common.exception.InvalidOperationException;
import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.exception.CounterpartyNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.exception.DuplicateCounterpartyException;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyAlreadyExistsException;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for all REST controllers
 */
@RestControllerAdvice(basePackages = "com.tarasantoniuk.finance")
public class GlobalExceptionHandler {

    // ========== GENERIC EXCEPTIONS ==========

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(
            InvalidOperationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(
            InsufficientBalanceException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    // ========== DOCUMENT EXCEPTIONS ==========

    @ExceptionHandler(InvalidDocumentStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDocumentStatus(
            InvalidDocumentStatusException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ========== COUNTERPARTY EXCEPTIONS ==========

    @ExceptionHandler(CounterpartyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCounterpartyNotFoundException(
            CounterpartyNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateCounterpartyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCounterpartyException(
            DuplicateCounterpartyException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ========== COUNTRY EXCEPTIONS ==========

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCountryNotFoundException(
            CountryNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ========== CURRENCY EXCEPTIONS ==========

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyNotFoundException(
            CurrencyNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CurrencyAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyAlreadyExistsException(
            CurrencyAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ========== VALIDATION EXCEPTIONS ==========

    @ExceptionHandler(jakarta.validation.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleJakartaValidationException(
            jakarta.validation.ValidationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(com.tarasantoniuk.finance.common.exception.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleCustomValidationException(
            com.tarasantoniuk.finance.common.exception.ValidationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Invalid input data",
                request.getRequestURI()
        );

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // ========== GLOBAL EXCEPTION ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request
        );
    }

    // ========== HELPER METHODS ==========

    /**
     * Builds standardized error response
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}