package com.springboot.resend.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Error {
    // Errores de autenticación y autorización
    UNAUTHORIZED("Falta de credenciales o token inválido", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("Acceso denegado", HttpStatus.FORBIDDEN),

    // Errores de validación
    INVALID_INPUT("Entrada inválida", HttpStatus.BAD_REQUEST),

    // Errores de negocio
    DUPLICATED_USERNAME("El nombre de usuario ya está en uso. Introduce otro", HttpStatus.UNPROCESSABLE_ENTITY),

    // Errores de recursos no encontrados
    USER_NOT_FOUND("Usuario no encontrado", HttpStatus.NOT_FOUND),

    // Error genérico del servidor
    INTERNAL_SERVER_ERROR("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("Error de conexión", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;
}
