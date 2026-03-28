package com.grocerystore.grocery_store.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            ex.message,
            request.getDescription(false),
            "RESOURCE_NOT_FOUND"
        )
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND)
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    ResponseEntity<ErrorDetails> handleInsufficientStockException(InsufficientStockException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            ex.message,
            request.getDescription(false),
            "INSUFFICIENT_STOCK"
        )
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            ex.message ?: "An unexpected error occurred",
            request.getDescription(false),
            "INTERNAL_SERVER_ERROR"
        )
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

class ErrorDetails {
    LocalDateTime timestamp
    String message
    String details
    String errorCode

    ErrorDetails(LocalDateTime timestamp, String message, String details, String errorCode) {
        this.timestamp = timestamp
        this.message = message
        this.details = details
        this.errorCode = errorCode
    }
}
