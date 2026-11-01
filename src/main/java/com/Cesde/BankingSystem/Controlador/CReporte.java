package com.Cesde.BankingSystem.Controlador;

import com.Cesde.BankingSystem.Dto.ReporteCompraTarjeta;
import com.Cesde.BankingSystem.Dto.ReporteCuenta;
import com.Cesde.BankingSystem.Dto.ReporteMovimiento;
import com.Cesde.BankingSystem.Dto.ReportePersona;
import com.Cesde.BankingSystem.Dto.ReporteTarjeta;
import com.Cesde.BankingSystem.Servicio.SReporte;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controlador REST de SOLO LECTURA que consume el script de Python
// (python/reporte_banking.py) para generar las graficas con pandas y
// matplotlib. No pide inicio de sesion, por eso solo devuelve datos no
// sensibles (ver los DTOs Reporte*).
@RestController
@RequestMapping("/api/reportes")
public class CReporte {
    @Autowired
    SReporte sReporte;

    // GET /api/reportes/cuentas
    @GetMapping("/cuentas")
    public ResponseEntity<List<ReporteCuenta>> cuentas() throws Exception {
        return ResponseEntity.ok(sReporte.listarCuentas());
    }

    // GET /api/reportes/movimientos
    @GetMapping("/movimientos")
    public ResponseEntity<List<ReporteMovimiento>> movimientos() throws Exception {
        return ResponseEntity.ok(sReporte.listarMovimientos());
    }

    // GET /api/reportes/tarjetas
    @GetMapping("/tarjetas")
    public ResponseEntity<List<ReporteTarjeta>> tarjetas() throws Exception {
        return ResponseEntity.ok(sReporte.listarTarjetas());
    }

    // GET /api/reportes/compras-tarjeta
    @GetMapping("/compras-tarjeta")
    public ResponseEntity<List<ReporteCompraTarjeta>> comprasTarjeta() throws Exception {
        return ResponseEntity.ok(sReporte.listarComprasTarjeta());
    }

    // GET /api/reportes/personas
    @GetMapping("/personas")
    public ResponseEntity<List<ReportePersona>> personas() throws Exception {
        return ResponseEntity.ok(sReporte.listarPersonas());
    }
}
