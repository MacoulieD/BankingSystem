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
import com.Cesde.BankingSystem.Servicio.SMovimiento;

// Convierte las entidades en los DTOs de reportes (solo datos no sensibles)
public final class ReporteMapper {

    private ReporteMapper() {
    }

    public static ReporteCuenta toCuenta(MCuenta cuenta) {
        return new ReporteCuenta(
                cuenta.getNumerocuenta(),
                cuenta instanceof MCuentaCorriente ? CuentaMapper.TIPO_CORRIENTE : CuentaMapper.TIPO_AHORRO,
                cuenta.getSaldo(),
                cuenta instanceof MCuentaCorriente corriente ? corriente.getSobregiro() : 0.0,
                cuenta.getEstado(),
                cuenta.getCreatedat());
    }

    public static ReporteMovimiento toMovimiento(MMovimiento movimiento) {
        return new ReporteMovimiento(
                movimiento.getId(),
                movimiento.getNumerocuenta(),
                movimiento.getTipomovimiento(),
                movimiento.getMonto(),
                movimiento.getFecha(),
                destino(movimiento));
    }

    // A donde se envio una transferencia, sacado de su descripcion
    // ("Transferencia a Nequi ****4567"). No devuelve usuarios ni numeros.
    private static String destino(MMovimiento movimiento) {
        if (!SMovimiento.TRANSFERENCIA_SALIDA.equals(movimiento.getTipomovimiento())) {
            return null;
        }
        String descripcion = movimiento.getDescripcion() == null ? "" : movimiento.getDescripcion();
        if (descripcion.startsWith("Transferencia a tu cuenta")) {
            return "Entre mis cuentas";
        }
        for (String banco : SMovimiento.BILLETERAS) {
            if (descripcion.startsWith("Transferencia a " + banco + " ")) {
                return banco;
            }
        }
        for (String banco : SMovimiento.BANCOS) {
            if (descripcion.startsWith("Transferencia a " + banco + " ")) {
                return banco;
            }
        }
        return "Otro usuario MiPlata";
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
