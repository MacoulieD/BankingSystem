package com.Cesde.BankingSystem.Dto;

import java.time.LocalDateTime;

// Compra (card-purchase) o pago (card-payment) de la tarjeta.
// capital, installments, monthlyPayment y remaining solo vienen en las compras:
// el frontend los usa para calcular la "cuota del mes".
public record MovimientoTarjetaResponse(
        String id,
        String type,
        String description,
        Double amount,
        Double debtAfter,
        LocalDateTime date,
        Double capital,
        Integer installments,
        Double monthlyPayment,
        Double remaining
) {
}
