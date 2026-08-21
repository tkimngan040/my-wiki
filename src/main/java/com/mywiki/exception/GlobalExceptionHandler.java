package com.mywiki.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "message", "Validation failed",
                        "errors", errors
                )
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("message", exception.getMessage())
        );
    }

    @ExceptionHandler({
            WorkspaceNotFoundException.class,
            WorkspaceAccessDeniedException.class,
            WorkspaceNameAlreadyExistsException.class
    })
    public ResponseEntity<Map<String, String>> handleWorkspaceException(RuntimeException exception) {
        HttpStatus status = exception instanceof WorkspaceNotFoundException
                ? HttpStatus.NOT_FOUND
                : exception instanceof WorkspaceNameAlreadyExistsException
                ? HttpStatus.CONFLICT
                : HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }
}
