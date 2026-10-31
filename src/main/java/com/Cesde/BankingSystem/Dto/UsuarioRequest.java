package com.Cesde.BankingSystem.Dto;

// Operaciones que solo necesitan saber quien es el usuario (bloquear o cancelar la tarjeta)
public record UsuarioRequest(String username) {
}
