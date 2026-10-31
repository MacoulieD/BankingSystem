package com.Cesde.BankingSystem.Dto;

// Datos que envia el formulario de registro del frontend (RegisterPage.jsx).
// Los nombres de los campos son los mismos del formulario para que el JSON
// llegue directo sin tener que traducirlo.
public record RegistroRequest(
        String documentType,
        String documentNumber,
        String phone,
        String email,
        String fullName,
        String username,
        String password,
        String accountType,
        Long balance
) {
}
