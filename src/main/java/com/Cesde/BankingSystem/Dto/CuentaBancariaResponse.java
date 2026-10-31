package com.Cesde.BankingSystem.Dto;

import java.util.List;

// Una cuenta del usuario (ahorros "AH" o corriente "CC") con sus movimientos,
// del mas antiguo al mas reciente (el frontend los invierte para mostrarlos).
// overdraft: cupo de sobregiro (0 en ahorros); el usuario puede usar
// balance + overdraft, asi que balance puede quedar negativo en la corriente.
public record CuentaBancariaResponse(
        String accountType,
        String accountNumber,
        Double balance,
        Double overdraft,
        List<MovimientoResponse> transactions
) {
}
