package com.Cesde.BankingSystem.Dto;

// Solicitar la tarjeta de credito: cuenta asociada y cupo solicitado
public record SolicitarTarjetaRequest(String username, String accountNumber, Double limit) {
}
