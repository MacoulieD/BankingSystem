package com.Cesde.BankingSystem.Controlador;

import com.Cesde.BankingSystem.Dto.CompraTarjetaRequest;
import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.PagoTarjetaRequest;
import com.Cesde.BankingSystem.Dto.SolicitarTarjetaRequest;
import com.Cesde.BankingSystem.Dto.UsuarioRequest;
import com.Cesde.BankingSystem.Servicio.STarjetaCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST de la tarjeta de credito (CreditCardPanel.jsx). Cada
// operacion devuelve el usuario completo con la tarjeta actualizada.
@RestController
@RequestMapping("/api/tarjeta")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CTarjeta {
    @Autowired
    STarjetaCredito sTarjetaCredito;

    // POST /api/tarjeta/solicitar
    @PostMapping("/solicitar")
    public ResponseEntity<CuentaResponse> solicitar(@RequestBody SolicitarTarjetaRequest datos) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(sTarjetaCredito.solicitar(datos));
    }

    // POST /api/tarjeta/compra
    @PostMapping("/compra")
    public ResponseEntity<CuentaResponse> comprar(@RequestBody CompraTarjetaRequest datos) throws Exception {
        return ResponseEntity.ok(sTarjetaCredito.comprar(datos));
    }

    // POST /api/tarjeta/pago
    @PostMapping("/pago")
    public ResponseEntity<CuentaResponse> pagar(@RequestBody PagoTarjetaRequest datos) throws Exception {
        return ResponseEntity.ok(sTarjetaCredito.pagar(datos));
    }

    // PUT /api/tarjeta/bloquear
    @PutMapping("/bloquear")
    public ResponseEntity<CuentaResponse> bloquear(@RequestBody UsuarioRequest datos) throws Exception {
        return ResponseEntity.ok(sTarjetaCredito.bloquear(datos));
    }

    // POST /api/tarjeta/cancelar
    @PostMapping("/cancelar")
    public ResponseEntity<CuentaResponse> cancelar(@RequestBody UsuarioRequest datos) throws Exception {
        return ResponseEntity.ok(sTarjetaCredito.cancelar(datos));
    }
}
