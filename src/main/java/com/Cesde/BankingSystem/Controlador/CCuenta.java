package com.Cesde.BankingSystem.Controlador;

import com.Cesde.BankingSystem.Dto.AbrirCuentaRequest;
import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.DestinoResponse;
import com.Cesde.BankingSystem.Servicio.SCuenta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST de las cuentas del usuario: abrir otra cuenta y buscar
// la cuenta de destino de una transferencia (TransferPanel.jsx)
@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CCuenta {
    @Autowired
    SCuenta sCuenta;

    // POST /api/cuentas -> abre otra cuenta (maximo una de cada tipo) y devuelve el usuario
    @PostMapping
    public ResponseEntity<CuentaResponse> abrirCuenta(@RequestBody AbrirCuentaRequest datos) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(sCuenta.abrirCuenta(datos));
    }

    // GET /api/cuentas/destino?numero=1234567890 -> titular de una cuenta MiPlata
    @GetMapping("/destino")
    public ResponseEntity<DestinoResponse> destino(@RequestParam String numero) throws Exception {
        return ResponseEntity.ok(sCuenta.buscarDestino(numero));
    }

    // GET /api/cuentas/destino-celular?celular=3001234567 -> titular por celular
    @GetMapping("/destino-celular")
    public ResponseEntity<DestinoResponse> destinoPorCelular(@RequestParam String celular) throws Exception {
        return ResponseEntity.ok(sCuenta.buscarDestinoPorCelular(celular));
    }
}
