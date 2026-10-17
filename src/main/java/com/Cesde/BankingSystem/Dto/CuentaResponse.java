package com.Cesde.BankingSystem.Dto;

import java.util.List;

// Respuesta del registro y del login: persona + cuenta con la misma forma del
// objeto "account" que usa el frontend. Nunca incluye la contrasena.
public record CuentaResponse(
        String documentType,
        String documentNumber,
        String phone,
        String email,
        String fullName,
        String username,
        String accountType,
        String accountNumber,
        Double balance,
        List<MovimientoResponse> transactions
) {
}
