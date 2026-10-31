package com.Cesde.BankingSystem.Dto;

// Persona para los reportes de Python: solo el estado de la cuenta de
// usuario. NUNCA incluye nombre, documento, correo ni contrasena.
public record ReportePersona(
        int idperson,
        Boolean activo,
        int failedAttempts
) {
}
