package com.Cesde.BankingSystem.Dto;

import java.time.LocalDateTime;

// Compra con tarjeta para los reportes de Python. No incluye el comercio
// ni datos de la tarjeta: solo los valores para graficar cuotas e intereses.
public record ReporteCompraTarjeta(
        int id,
        int idtarjeta,
        Integer cuotas,
        Double capital,
        Double total,
        LocalDateTime fecha
) {
}
