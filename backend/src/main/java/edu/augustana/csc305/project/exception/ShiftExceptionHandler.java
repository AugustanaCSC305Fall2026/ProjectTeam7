package edu.augustana.csc305.project.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ShiftExceptionHandler {

    @ExceptionHandler(ShiftException.class)
    public ResponseEntity<Map<String, String>> handleShiftException(
            ShiftException exception) {

        return ResponseEntity
                .status(exception.getStatus())
                .body(Map.of("error", exception.getCode()));
    }
}