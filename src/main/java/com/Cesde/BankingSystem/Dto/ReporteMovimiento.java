package com.Cesde.BankingSystem.Dto;

import java.time.LocalDateTime;

// Movimiento para los reportes de Python: tipo, monto, fecha y, en las
// transferencias enviadas, a donde se envio (banco, billetera o MiPlata)
public record ReporteMovimiento(
        int id,
        String numerocuenta,
        String tipomovimiento,
        Double monto,
        LocalDateTime fecha,
        String destino
) {
}
