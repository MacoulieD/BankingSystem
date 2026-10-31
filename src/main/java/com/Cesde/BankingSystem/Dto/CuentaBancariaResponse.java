package com.Cesde.BankingSystem.Dto;

import java.util.List;

// Una cuenta del usuario (ahorros "AH" o corriente "CC") con sus movimientos,
// del mas antiguo al mas reciente (el frontend los invierte para mostrarlos)
public record CuentaBancariaResponse(
        String accountType,
        String accountNumber,
        Double balance,
        List<MovimientoResponse> transactions
) {
}
