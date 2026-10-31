package com.Cesde.BankingSystem.Dto;

// Datos de contacto que el usuario puede editar desde su perfil
public record ActualizarContactoRequest(String username, String phone, String email) {
}
