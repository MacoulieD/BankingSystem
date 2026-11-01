package com.Cesde.BankingSystem.Dto;

import java.time.LocalDateTime;

// Cuenta para los reportes de Python (python/reporte_banking.py).
// Solo datos para graficar: no incluye propietario ni datos personales.
public record ReporteCuenta(
        String numerocuenta,
        String tipocuenta,
        Double saldo,
        // cupo de sobregiro: solo las cuentas corrientes, en ahorros es 0
        Double sobregiro,
        Boolean estado,
        LocalDateTime createdat
) {
}
