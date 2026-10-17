package com.Cesde.BankingSystem.Mapper;

import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.MovimientoResponse;
import com.Cesde.BankingSystem.Dto.RegistroRequest;
import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MCuentaAhorro;
import com.Cesde.BankingSystem.Modelo.MCuentaCorriente;
import com.Cesde.BankingSystem.Modelo.MPerson;

import java.time.LocalDateTime;
import java.util.List;

// Conversiones de la cuenta:
//  - RegistroRequest -> MCuentaAhorro o MCuentaCorriente (segun accountType)
//  - MPerson + MCuenta + movimientos -> CuentaResponse (lo que recibe el frontend)
public final class CuentaMapper {
    public static final String TIPO_AHORRO = "AH";
    public static final String TIPO_CORRIENTE = "CC";
    private static final double TASA_INTERES_AHORRO = 0.5;
    private static final double SOBREGIRO_CORRIENTE = 0.0;

    private CuentaMapper() {
    }

    // numeroCuenta lo genera SPerson porque necesita consultar la BD para que no se repita
    public static MCuenta toEntity(RegistroRequest datos, MPerson persona, String numeroCuenta) {
        MCuenta cuenta;
        if (TIPO_CORRIENTE.equals(MapperUtils.texto(datos.accountType()))) {
            MCuentaCorriente corriente = new MCuentaCorriente();
            corriente.setSobregiro(SOBREGIRO_CORRIENTE);
            cuenta = corriente;
        } else {
            MCuentaAhorro ahorro = new MCuentaAhorro();
            ahorro.setTasainteres(TASA_INTERES_AHORRO);
            cuenta = ahorro;
        }
        cuenta.setNumerocuenta(numeroCuenta);
        cuenta.setPropietario(persona.getUsername());
        cuenta.setSaldo(datos.balance().doubleValue());
        cuenta.setEstado(true);
        cuenta.setCreatedat(LocalDateTime.now());
        cuenta.setPersona(persona);
        return cuenta;
    }

    public static CuentaResponse toResponse(MPerson persona, MCuenta cuenta, List<MovimientoResponse> movimientos) {
        return new CuentaResponse(
                persona.getTipodocumento(),
                persona.getDocumento(),
                persona.getTelephone(),
                persona.getEmail(),
                persona.getName(),
                persona.getUsername(),
                cuenta instanceof MCuentaCorriente ? TIPO_CORRIENTE : TIPO_AHORRO,
                cuenta.getNumerocuenta(),
                cuenta.getSaldo(),
                movimientos);
    }
}
