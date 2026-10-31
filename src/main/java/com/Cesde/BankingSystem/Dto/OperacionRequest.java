package com.Cesde.BankingSystem.Dto;

// Consignar o retirar en una cuenta del usuario
public record OperacionRequest(String username, String accountNumber, Double amount) {
}
