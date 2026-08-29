package com.mywiki.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "VALIDATION_ERROR");
        body.put("message", "Validation failed");
        body.put("details", errors);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "CONFLICT",
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHENTICATED",
                "Invalid email or password",
                request,
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Access denied",
                request,
                null
        );
    }

    @ExceptionHandler({
            WorkspaceNotFoundException.class,
            WorkspaceAccessDeniedException.class,
            WorkspaceNameAlreadyExistsException.class
    })
    public ResponseEntity<Map<String, Object>> handleWorkspaceException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = exception instanceof WorkspaceNotFoundException
                ? HttpStatus.NOT_FOUND
                : exception instanceof WorkspaceNameAlreadyExistsException
                ? HttpStatus.CONFLICT
                : HttpStatus.FORBIDDEN;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND"
                : status == HttpStatus.CONFLICT ? "CONFLICT" : "ACCESS_DENIED";
        return buildErrorResponse(status, code, exception.getMessage(), request, null);
    }

    @ExceptionHandler({
            FolderNotFoundException.class,
            FolderAccessDeniedException.class,
            FolderNameAlreadyExistsException.class,
            InvalidFolderMoveException.class
    })
    public ResponseEntity<Map<String, Object>> handleFolderException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = exception instanceof FolderNotFoundException
                ? HttpStatus.NOT_FOUND
                : exception instanceof FolderNameAlreadyExistsException
                ? HttpStatus.CONFLICT
                : exception instanceof FolderAccessDeniedException
                ? HttpStatus.FORBIDDEN
                : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND"
                : status == HttpStatus.CONFLICT ? "CONFLICT"
                : status == HttpStatus.FORBIDDEN ? "ACCESS_DENIED"
                : "VALIDATION_ERROR";
        return buildErrorResponse(status, code, exception.getMessage(), request, null);
    }

    @ExceptionHandler({
            PageNotFoundException.class,
            PageNameAlreadyExistsException.class,
            PageAccessDeniedException.class
    })
    public ResponseEntity<Map<String, Object>> handlePageException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = exception instanceof PageNotFoundException
                ? HttpStatus.NOT_FOUND
                : exception instanceof PageNameAlreadyExistsException
                ? HttpStatus.CONFLICT
                : HttpStatus.FORBIDDEN;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND"
                : status == HttpStatus.CONFLICT ? "CONFLICT" : "ACCESS_DENIED";
        return buildErrorResponse(status, code, exception.getMessage(), request, null);
    }

    @ExceptionHandler({
            LinkNotFoundException.class,
            LinkAccessDeniedException.class,
            InvalidLinkException.class
    })
    public ResponseEntity<Map<String, Object>> handleLinkException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = exception instanceof LinkNotFoundException
                ? HttpStatus.NOT_FOUND
                : exception instanceof LinkAccessDeniedException
                ? HttpStatus.FORBIDDEN
                : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND"
                : status == HttpStatus.FORBIDDEN ? "ACCESS_DENIED" : "VALIDATION_ERROR";
        return buildErrorResponse(status, code, exception.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Map<String, Object>> handleMalformedRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request contains invalid data",
                request,
                null
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> details
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
