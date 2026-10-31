package com.Cesde.BankingSystem.Mapper;

import com.Cesde.BankingSystem.Dto.MovimientoResponse;
import com.Cesde.BankingSystem.Modelo.MMovimiento;

import java.util.List;

// Convierte los movimientos de la BD al formato "transactions" del dashboard
public final class MovimientoMapper {

    private MovimientoMapper() {
    }

    public static MovimientoResponse toResponse(MMovimiento movimiento) {
        return new MovimientoResponse(
                String.valueOf(movimiento.getId()),
                movimiento.getTipomovimiento(),
                movimiento.getDescripcion(),
                movimiento.getMonto(),
                movimiento.getSaldoposterior(),
                movimiento.getFecha());
    }

    public static List<MovimientoResponse> toResponseList(List<MMovimiento> movimientos) {
        return movimientos.stream().map(MovimientoMapper::toResponse).toList();
    }
}
