package com.sisi414.parcial.exception;

/** Recurso inexistente (404). */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
