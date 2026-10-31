package com.Cesde.BankingSystem.Controlador;

import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.OperacionRequest;
import com.Cesde.BankingSystem.Dto.TransferenciaRequest;
import com.Cesde.BankingSystem.Servicio.SMovimiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST de las operaciones con dinero del dashboard. Cada una
// guarda el movimiento en la BD y devuelve el usuario con sus saldos al dia.
@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CMovimiento {
    @Autowired
    SMovimiento sMovimiento;

    // POST /api/movimientos/consignar
    @PostMapping("/consignar")
    public ResponseEntity<CuentaResponse> consignar(@RequestBody OperacionRequest datos) throws Exception {
        return ResponseEntity.ok(sMovimiento.consignar(datos));
    }

    // POST /api/movimientos/retirar
    @PostMapping("/retirar")
    public ResponseEntity<CuentaResponse> retirar(@RequestBody OperacionRequest datos) throws Exception {
        return ResponseEntity.ok(sMovimiento.retirar(datos));
    }

    // POST /api/movimientos/transferir
    @PostMapping("/transferir")
    public ResponseEntity<CuentaResponse> transferir(@RequestBody TransferenciaRequest datos) throws Exception {
        return ResponseEntity.ok(sMovimiento.transferir(datos));
    }
}
