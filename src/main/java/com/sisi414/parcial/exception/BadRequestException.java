package com.sisi414.parcial.exception;

/** Dato invalido o referencia inexistente (400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
