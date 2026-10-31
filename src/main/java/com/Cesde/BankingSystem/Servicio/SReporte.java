package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Dto.ReporteCuenta;
import com.Cesde.BankingSystem.Dto.ReporteMovimiento;
import com.Cesde.BankingSystem.Dto.ReportePersona;
import com.Cesde.BankingSystem.Dto.ReporteTarjeta;
import com.Cesde.BankingSystem.Mapper.ReporteMapper;
import com.Cesde.BankingSystem.Repositorio.ICuenta;
import com.Cesde.BankingSystem.Repositorio.IMovimiento;
import com.Cesde.BankingSystem.Repositorio.IPerson;
import com.Cesde.BankingSystem.Repositorio.ITarjetaCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Servicio de reportes: solo LEE la BD y devuelve los datos ya convertidos
// a DTOs de reporte para que el script de Python los grafique.
@Service
public class SReporte {

    @Autowired
    ICuenta iCuenta;

    @Autowired
    IMovimiento iMovimiento;

    @Autowired
    ITarjetaCredito iTarjetaCredito;

    @Autowired
    IPerson iPerson;

    // Todas las cuentas (ahorro y corriente)
    public List<ReporteCuenta> listarCuentas() throws Exception {
        try {
            return iCuenta.findAll().stream().map(ReporteMapper::toCuenta).toList();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Todos los movimientos
    public List<ReporteMovimiento> listarMovimientos() throws Exception {
        try {
            return iMovimiento.findAll().stream().map(ReporteMapper::toMovimiento).toList();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Todas las tarjetas (sin numero ni CVV)
    public List<ReporteTarjeta> listarTarjetas() throws Exception {
        try {
            return iTarjetaCredito.findAll().stream().map(ReporteMapper::toTarjeta).toList();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Todas las personas (solo estado activo/bloqueado)
    public List<ReportePersona> listarPersonas() throws Exception {
        try {
            return iPerson.findAll().stream().map(ReporteMapper::toPersona).toList();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }
}
