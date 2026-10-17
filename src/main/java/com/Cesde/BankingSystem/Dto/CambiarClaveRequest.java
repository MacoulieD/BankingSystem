package com.Cesde.BankingSystem.Dto;

// Cambio de clave desde el perfil: exige la clave actual
public record CambiarClaveRequest(String username, String currentPassword, String newPassword) {
}
