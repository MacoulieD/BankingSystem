package com.Cesde.BankingSystem.Controlador;

import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.LoginRequest;
import com.Cesde.BankingSystem.Dto.RegistroRequest;
import com.Cesde.BankingSystem.Servicio.SPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST que usa el frontend React (BankingSystemFrontd) para
// registrar usuarios e iniciar sesion. CrossOrigin permite las peticiones
// desde el servidor de desarrollo de Vite.
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CAutenticacion {
    @Autowired
    SPerson sPerson;

    // POST /api/registro -> crea persona + cuenta y devuelve 201
    @PostMapping("/registro")
    public ResponseEntity<CuentaResponse> registrar(@RequestBody RegistroRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sPerson.registrar(datos));
    }

    // POST /api/login -> devuelve los datos de la cuenta si las credenciales son correctas
    @PostMapping("/login")
    public ResponseEntity<CuentaResponse> iniciarSesion(@RequestBody LoginRequest datos) {
        return ResponseEntity.ok(sPerson.iniciarSesion(datos));
    }
}
