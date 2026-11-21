package com.sisi414.parcial.exception;

/** Duplicado o regla de integridad violada (409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
