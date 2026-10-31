package com.Cesde.BankingSystem.Mapper;

import com.Cesde.BankingSystem.Dto.MovimientoTarjetaResponse;
import com.Cesde.BankingSystem.Dto.TarjetaResponse;
import com.Cesde.BankingSystem.Modelo.MMovimientoTarjeta;
import com.Cesde.BankingSystem.Modelo.MTarjetaCredito;

import java.util.List;

// Convierte la tarjeta y su historial al formato "creditCard" del frontend
public final class TarjetaMapper {

    private TarjetaMapper() {
    }

    public static TarjetaResponse toResponse(MTarjetaCredito tarjeta, List<MMovimientoTarjeta> movimientos) {
        return new TarjetaResponse(
                enmascarar(tarjeta.getNumerotarjeta()),
                tarjeta.getFechavencimiento(),
                tarjeta.getLimitecredito(),
                tarjeta.getDeudaactual(),
                tarjeta.getActivo(),
                tarjeta.getCuenta().getNumerocuenta(),
                movimientos.stream().map(TarjetaMapper::toMovimiento).toList());
    }

    public static MovimientoTarjetaResponse toMovimiento(MMovimientoTarjeta movimiento) {
        return new MovimientoTarjetaResponse(
                String.valueOf(movimiento.getId()),
                movimiento.getTipo(),
                movimiento.getDescripcion(),
                movimiento.getMonto(),
                movimiento.getDeudaposterior(),
                movimiento.getFecha(),
                movimiento.getCapital(),
                movimiento.getCuotas(),
                movimiento.getValorcuota(),
                movimiento.getSaldopendiente());
    }

    // 4111222233334444 -> ************4444 (mismo largo, solo los ultimos 4 reales)
    public static String enmascarar(String numero) {
        return "*".repeat(numero.length() - 4) + ultimos4(numero);
    }

    public static String ultimos4(String numero) {
        return numero.substring(numero.length() - 4);
    }
}
