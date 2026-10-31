package com.Cesde.BankingSystem.Dto;

// Abrir otra cuenta desde el dashboard (DashboardPage.jsx): accountType "AH" o "CC"
public record AbrirCuentaRequest(String username, String accountType, Double balance) {
}
