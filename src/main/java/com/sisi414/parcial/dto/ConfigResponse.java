package com.sisi414.parcial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de salida de configuración (sin datos sensibles). */
public record ConfigResponse(
        Long id,
        String entidad,
        String descEnt,
        String unidad,
        String descUni,
        String usuario
) {
}
