package com.practicacarlos.devqa_microservicio.exception;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleNotFound(
            RecursoNoEncontradoException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler({DatoDuplicadoException.class, DuplicateKeyException.class})
    public ResponseEntity<ApiError> handleConflict(
            RuntimeException exception, HttpServletRequest request) {
        String message = exception instanceof DatoDuplicadoException
                ? exception.getMessage()
                : "El codigo ya está registrado";
        return response(HttpStatus.CONFLICT, message, request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> details = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos",
                request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> details = new TreeMap<>();
        exception.getConstraintViolations().forEach(violation ->
                details.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return response(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos",
                request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "El cuerpo JSON no es válido",
                request, Map.of());
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status, String message, HttpServletRequest request,
            Map<String, String> details) {
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
