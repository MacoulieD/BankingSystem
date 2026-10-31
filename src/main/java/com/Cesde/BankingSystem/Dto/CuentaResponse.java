package com.Cesde.BankingSystem.Dto;

import java.util.List;

// Respuesta del registro, del login y de TODAS las operaciones: la persona
// con todas sus cuentas y su tarjeta de credito (o null), con la misma forma
// del objeto "account" que usa el frontend. Nunca incluye la contrasena.
public record CuentaResponse(
        String documentType,
        String documentNumber,
        String phone,
        String email,
        String fullName,
        String username,
        List<CuentaBancariaResponse> bankAccounts,
        TarjetaResponse creditCard
) {
}
