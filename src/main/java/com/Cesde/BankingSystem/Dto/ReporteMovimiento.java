package com.Cesde.BankingSystem.Dto;

import java.time.LocalDateTime;

// Movimiento para los reportes de Python: tipo, monto y fecha
public record ReporteMovimiento(
        int id,
        String numerocuenta,
        String tipomovimiento,
        Double monto,
        LocalDateTime fecha
) {
}
