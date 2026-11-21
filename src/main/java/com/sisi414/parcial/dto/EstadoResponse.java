package com.sisi414.parcial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de salida de estado del bien (sin datos sensibles). */
public record EstadoResponse(
        Integer codEstado,
        String nomEstado
) {
}
