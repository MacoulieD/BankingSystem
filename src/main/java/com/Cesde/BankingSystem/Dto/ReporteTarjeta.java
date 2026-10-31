package com.Cesde.BankingSystem.Dto;

import java.time.LocalDate;

// Tarjeta para los reportes de Python. NUNCA incluye el numero de la
// tarjeta ni el CVV: la API de reportes no pide inicio de sesion.
public record ReporteTarjeta(
        int id,
        Double limitecredito,
        Double deudaactual,
        Boolean activo,
        LocalDate fechavencimiento
) {
}
