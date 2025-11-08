package dev.dsa.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle business exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("Business exception: {}", ex.getMessage(), ex);

        if (isApiRequest(request)) {
            return buildErrorResponse(ex, request, ex.getStatus());
        }

        return buildErrorView(ex.getUserMessage(), ex.getStatus().value());
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());

        if (isApiRequest(request)) {
            return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND);
        }

        return buildErrorView(ex.getUserMessage(), HttpStatus.NOT_FOUND.value());
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        if (isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message("Validation failed")
                    .path(request.getRequestURI())
                    .validationErrors(validationErrors)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }

        String errorMessage = validationErrors.stream()
                .map(e -> e.getField() + ": " + e.getMessage())
                .collect(Collectors.joining(", "));

        return buildErrorView("Validation failed: " + errorMessage, HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());

        if (isApiRequest(request)) {
            return buildErrorResponse(
                new BusinessException("Access denied", HttpStatus.FORBIDDEN),
                request,
                HttpStatus.FORBIDDEN
            );
        }

        return buildErrorView("You don't have permission to access this resource", HttpStatus.FORBIDDEN.value());
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        if (isApiRequest(request)) {
            return buildErrorResponse(
                new BusinessException("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
                request,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return buildErrorView("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * Check if request is an API request
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String requestUri = request.getRequestURI();

        return (acceptHeader != null && acceptHeader.contains("application/json"))
                || requestUri.startsWith("/api/");
    }

    /**
     * Build error response for API requests
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(BusinessException ex, HttpServletRequest request, HttpStatus status) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getUserMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Build error view for web requests
     */
    private ModelAndView buildErrorView(String message, int statusCode) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", message);
        mav.addObject("statusCode", statusCode);
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }
}
