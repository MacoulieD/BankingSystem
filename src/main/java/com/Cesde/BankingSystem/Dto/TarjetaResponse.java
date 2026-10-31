package com.Cesde.BankingSystem.Dto;

import java.time.LocalDate;
import java.util.List;

// Tarjeta de credito con la forma que espera CreditCardPanel.jsx.
// cardNumber va enmascarado (solo los ultimos 4 digitos reales) y el CVV
// NUNCA se envia.
public record TarjetaResponse(
        String cardNumber,
        LocalDate expiresAt,
        Double limit,
        Double debt,
        Boolean active,
        String accountNumber,
        List<MovimientoTarjetaResponse> transactions
) {
}
