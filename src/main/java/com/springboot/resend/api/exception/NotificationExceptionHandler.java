package com.springboot.resend.api.exception;

import com.springboot.resend.domain.exception.NotificationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class NotificationExceptionHandler {
    // Maneja errores genéricos
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorMessages> handleGenericException(Exception exception) {
        return responseErrorMessages(
                Map.of("INTERNAL_SERVER_ERROR", "Error interno del servidor"), // Error code, Error message
                HttpStatus.INTERNAL_SERVER_ERROR // Error http status
        );
    }

    // Maneja errores transitorios
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessages> handleRuntimeException(RuntimeException exception) {
        return responseErrorMessages(
            Map.of("INTERNAL_SERVER_ERROR", exception.getMessage()), // Error code, Error message
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // Maneja errores específicos
    @ExceptionHandler(NotificationException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessages> handleAppException(NotificationException exception) {
        return responseErrorMessages(
            Map.of(exception.getError().name(), exception.getMessage()), // Error code, Error message
            exception.getError().getStatus() // Error http status
        );
    }

    // Maneja errores de validación en los DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessages> handleValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> fieldError = exception.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField, // Error field
                FieldError::getDefaultMessage // Error message
            ));
        return responseErrorMessages(fieldError, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private ResponseEntity<ErrorMessages> responseErrorMessages(Map<String, String> error, HttpStatus status) {
        ErrorMessages errorMessages = new ErrorMessages();
        error.forEach(errorMessages::addError);
        return new ResponseEntity<>(errorMessages, status);
    }
}
