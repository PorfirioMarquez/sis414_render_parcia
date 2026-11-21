package com.sisi414.parcial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de salida de organismo financiador (sin datos sensibles). */
public record OrganismoFinResponse(
        Integer gestion,
        String codOf,
        String des,
        String sigla
) {
}
