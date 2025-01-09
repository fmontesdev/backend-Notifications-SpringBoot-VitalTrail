package com.springboot.notification.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Error {
    // Errores de autenticación y autorización
    UNAUTHORIZED("Acceso no autorizado a la API", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("Acceso denegado a la API", HttpStatus.FORBIDDEN),

    // Errores de validación
    INVALID_REQUEST("Solicitud inválida a la API", HttpStatus.BAD_REQUEST),

    // Errores de recursos no encontrados
    ENDPOINT_NOT_FOUND("Endpoint de la API no encontrado", HttpStatus.NOT_FOUND),

    // Error genérico del servidor
    INTERNAL_SERVER_ERROR("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("Error de conexión con la API", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;
}
