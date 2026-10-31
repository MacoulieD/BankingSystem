package com.Cesde.BankingSystem.Dto;

// Pago a la tarjeta de credito desde una cuenta del usuario
public record PagoTarjetaRequest(String username, String fromAccountNumber, Double amount) {
}
