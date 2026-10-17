package com.Cesde.BankingSystem.Controlador;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

// Convierte los errores en un JSON { "message": "..." } que el frontend
// muestra tal cual en la alerta del formulario.
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> errorDeNegocio(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode()).body(Map.of("message", error.getReason()));
    }

    // JSON mal formado (ej. un texto donde se esperaba el numero del saldo)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos() {
        return ResponseEntity.badRequest().body(Map.of("message", "Los datos enviados no son válidos."));
    }

    // por si dos registros iguales llegan al mismo tiempo y los saca el UNIQUE de la BD
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> datoRepetido() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "El usuario, el documento o el correo ya están registrados."));
    }
}
