package com.Cesde.BankingSystem.Dto;

// Compra con la tarjeta de credito a cuotas (1 a 36)
public record CompraTarjetaRequest(String username, String merchant, Double amount, Integer installments) {
}
