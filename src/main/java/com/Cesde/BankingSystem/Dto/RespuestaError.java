package com.Cesde.BankingSystem.Dto;

import java.time.LocalDateTime;

// Formato unico de las respuestas de error de la API. Ejemplo:
// {
//   "fecha": "2026-10-21T10:15:30",
//   "codigo": 409,
//   "error": "Conflict",
//   "message": "Ese nombre de usuario ya está registrado.",
//   "ruta": "/api/registro"
// }
// El campo se llama "message" (y no "mensaje") a proposito: es el que ya lee
// el frontend para mostrar la alerta, asi que sigue funcionando sin cambios.
public record RespuestaError(
        LocalDateTime fecha,
        int codigo,
        String error,
        String message,
        String ruta
) {
}
