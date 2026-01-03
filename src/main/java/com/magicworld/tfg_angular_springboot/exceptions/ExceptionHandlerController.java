package com.magicworld.tfg_angular_springboot.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionHandlerController {

    private final Environment env;

    public ExceptionHandlerController(Environment env) {
        this.env = env;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorMessage> handleApiException(ApiException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseStatus rs = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (rs != null) {
            status = rs.code();
        }
        ErrorMessage body = new ErrorMessage(
                status.value(),
                new Date(),
                ex.getCode(),
                ex.getArgs(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        ErrorMessage body = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                "error.validation",
                new Object[]{ errors },
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessage> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        ErrorMessage body = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                "error.validation",
                new Object[]{ errors },
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorMessage> handleMaxUpload(MaxUploadSizeExceededException ex, WebRequest request) {
        String configured = env.getProperty("spring.servlet.multipart.max-file-size", "20MB");
        long maxBytes = parseSizeToBytes(configured, 20L * 1024L * 1024L);
        ErrorMessage body = new ErrorMessage(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                new Date(),
                "error.file.size_exceeded",
                new Object[]{ maxBytes },
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorMessage> handleMultipartException(MultipartException ex, WebRequest request) {
        Throwable cause = ex.getCause();
        if (cause instanceof MaxUploadSizeExceededException) {
            return handleMaxUpload((MaxUploadSizeExceededException) cause, request);
        }
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("maximum upload size exceeded")) {
            String configured = env.getProperty("spring.servlet.multipart.max-file-size", "20MB");
            long maxBytes = parseSizeToBytes(configured, 20L * 1024L * 1024L);
            return handleMaxUpload(new MaxUploadSizeExceededException(maxBytes), request);
        }
        ErrorMessage body = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                "error.internal",
                new Object[]{},
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleAllUncaught(Exception ex, WebRequest request) {
        ErrorMessage body = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new Date(),
                "error.internal",
                new Object[]{},
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static final Map<String, Long> SIZE_MULTIPLIERS = Map.of(
            "KB", 1024L,
            "MB", 1024L * 1024L,
            "GB", 1024L * 1024L * 1024L
    );

    private long parseSizeToBytes(String s, long defaultValue) {
        if (s == null || s.isBlank()) return defaultValue;
        String t = s.trim().toUpperCase();

        try {
            for (Map.Entry<String, Long> entry : SIZE_MULTIPLIERS.entrySet()) {
                if (t.endsWith(entry.getKey())) {
                    long v = Long.parseLong(t.substring(0, t.length() - entry.getKey().length()).trim());
                    return v * entry.getValue();
                }
            }

            if (t.endsWith("B")) {
                return Long.parseLong(t.substring(0, t.length() - 1).trim());
            }

            return Long.parseLong(t);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
