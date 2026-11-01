package com.Cesde.BankingSystem.Mapper;

import com.Cesde.BankingSystem.Dto.ReporteCompraTarjeta;
import com.Cesde.BankingSystem.Dto.ReporteCuenta;
import com.Cesde.BankingSystem.Dto.ReporteMovimiento;
import com.Cesde.BankingSystem.Dto.ReportePersona;
import com.Cesde.BankingSystem.Dto.ReporteTarjeta;
import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MCuentaCorriente;
import com.Cesde.BankingSystem.Modelo.MMovimiento;
import com.Cesde.BankingSystem.Modelo.MMovimientoTarjeta;
import com.Cesde.BankingSystem.Modelo.MPerson;
import com.Cesde.BankingSystem.Modelo.MTarjetaCredito;

// Convierte las entidades en los DTOs de reportes (solo datos no sensibles)
public final class ReporteMapper {

    private ReporteMapper() {
    }

    public static ReporteCuenta toCuenta(MCuenta cuenta) {
        return new ReporteCuenta(
                cuenta.getNumerocuenta(),
                cuenta instanceof MCuentaCorriente ? CuentaMapper.TIPO_CORRIENTE : CuentaMapper.TIPO_AHORRO,
                cuenta.getSaldo(),
                cuenta.getEstado(),
                cuenta.getCreatedat());
    }

    public static ReporteMovimiento toMovimiento(MMovimiento movimiento) {
        return new ReporteMovimiento(
                movimiento.getId(),
                movimiento.getNumerocuenta(),
                movimiento.getTipomovimiento(),
                movimiento.getMonto(),
                movimiento.getFecha());
    }

    public static ReporteTarjeta toTarjeta(MTarjetaCredito tarjeta) {
        return new ReporteTarjeta(
                tarjeta.getId(),
                tarjeta.getLimitecredito(),
                tarjeta.getDeudaactual(),
                tarjeta.getActivo(),
                tarjeta.getFechavencimiento());
    }

    public static ReporteCompraTarjeta toCompraTarjeta(MMovimientoTarjeta compra) {
        return new ReporteCompraTarjeta(
                compra.getId(),
                compra.getTarjeta().getId(),
                compra.getCuotas(),
                compra.getCapital(),
                compra.getMonto(),
                compra.getFecha());
    }

    public static ReportePersona toPersona(MPerson persona) {
        return new ReportePersona(
                persona.getIdperson(),
                persona.getActivo(),
                persona.getFailedAttempts());
    }
}
