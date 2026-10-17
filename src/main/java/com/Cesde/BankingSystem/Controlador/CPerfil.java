package com.Cesde.BankingSystem.Controlador;

import com.Cesde.BankingSystem.Dto.ActualizarContactoRequest;
import com.Cesde.BankingSystem.Dto.CambiarClaveRequest;
import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Servicio.SPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Controlador REST del perfil: el usuario solo puede editar celular, correo y clave
@RestController
@RequestMapping("/api/perfil")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CPerfil {
    @Autowired
    SPerson sPerson;

    // PUT /api/perfil/contacto -> actualiza celular y correo, devuelve la cuenta actualizada
    @PutMapping("/contacto")
    public ResponseEntity<CuentaResponse> actualizarContacto(@RequestBody ActualizarContactoRequest datos) {
        return ResponseEntity.ok(sPerson.actualizarContacto(datos));
    }

    // PUT /api/perfil/clave -> cambia la clave si la clave actual es correcta
    @PutMapping("/clave")
    public ResponseEntity<Map<String, String>> cambiarClave(@RequestBody CambiarClaveRequest datos) {
        sPerson.cambiarClave(datos);
        return ResponseEntity.ok(Map.of("message", "Clave actualizada correctamente."));
    }
}
