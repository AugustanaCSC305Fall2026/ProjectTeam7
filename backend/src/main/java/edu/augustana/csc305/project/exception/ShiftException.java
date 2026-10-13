package edu.augustana.csc305.project.exception;

import org.springframework.http.HttpStatus;

public class ShiftException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ShiftException(HttpStatus status, String code) {
        super(code);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}