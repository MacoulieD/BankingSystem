package com.Cesde.BankingSystem.Dto;

import java.time.LocalDateTime;

// Movimiento con la forma que espera el dashboard del frontend (transactions)
public record MovimientoResponse(
        String id,
        String type,
        String description,
        Double amount,
        Double balanceAfter,
        LocalDateTime date
) {
}
