package com.Cesde.BankingSystem.Mapper;

import com.Cesde.BankingSystem.Dto.CuentaBancariaResponse;
import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.TarjetaResponse;
import com.Cesde.BankingSystem.Dto.MovimientoResponse;
import com.Cesde.BankingSystem.Dto.RegistroRequest;
import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MCuentaAhorro;
import com.Cesde.BankingSystem.Modelo.MCuentaCorriente;
import com.Cesde.BankingSystem.Modelo.MPerson;

import java.time.LocalDateTime;
import java.util.List;

// Conversiones de la cuenta:
//  - RegistroRequest / AbrirCuentaRequest -> MCuentaAhorro o MCuentaCorriente (segun accountType)
//  - MPerson + cuentas + tarjeta -> CuentaResponse (lo que recibe el frontend)
public final class CuentaMapper {
    public static final String TIPO_AHORRO = "AH";
    public static final String TIPO_CORRIENTE = "CC";
    private static final double TASA_INTERES_AHORRO = 0.5;
    // la cuenta corriente puede quedar en negativo hasta el 20% del saldo con
    // que se abrio (cupo fijo, como el que aprueba un banco)
    public static final double PORCENTAJE_SOBREGIRO = 0.20;

    private CuentaMapper() {
    }

    // numeroCuenta lo genera SPerson porque necesita consultar la BD para que no se repita
    public static MCuenta toEntity(RegistroRequest datos, MPerson persona, String numeroCuenta) {
        return toEntity(datos.accountType(), datos.balance(), persona, numeroCuenta);
    }

    // Tambien la usa SCuenta.abrirCuenta (segunda cuenta del usuario)
    public static MCuenta toEntity(String tipoCuenta, Long saldo, MPerson persona, String numeroCuenta) {
        MCuenta cuenta;
        if (TIPO_CORRIENTE.equals(MapperUtils.texto(tipoCuenta))) {
            MCuentaCorriente corriente = new MCuentaCorriente();
            // entero (pesos completos): 20% de 50.001 -> 10.000
            corriente.setSobregiro(Math.floor(saldo * PORCENTAJE_SOBREGIRO));
            cuenta = corriente;
        } else {
            MCuentaAhorro ahorro = new MCuentaAhorro();
            ahorro.setTasainteres(TASA_INTERES_AHORRO);
            cuenta = ahorro;
        }
        cuenta.setNumerocuenta(numeroCuenta);
        cuenta.setPropietario(persona.getUsername());
        cuenta.setSaldo(saldo.doubleValue());
        cuenta.setEstado(true);
        cuenta.setCreatedat(LocalDateTime.now());
        cuenta.setPersona(persona);
        return cuenta;
    }

    // tarjeta puede ser null (el usuario no tiene tarjeta de credito)
    public static CuentaResponse toResponse(MPerson persona, List<CuentaBancariaResponse> cuentas, TarjetaResponse tarjeta) {
        return new CuentaResponse(
                persona.getTipodocumento(),
                persona.getDocumento(),
                persona.getTelephone(),
                persona.getEmail(),
                persona.getName(),
                persona.getUsername(),
                cuentas,
                tarjeta);
    }

    public static CuentaBancariaResponse toCuentaBancaria(MCuenta cuenta, List<MovimientoResponse> movimientos) {
        double sobregiro = cuenta instanceof MCuentaCorriente corriente ? corriente.getSobregiro() : 0.0;
        return new CuentaBancariaResponse(tipo(cuenta), cuenta.getNumerocuenta(), cuenta.getSaldo(), sobregiro, movimientos);
    }

    // "AH" o "CC" segun la clase concreta de la cuenta
    public static String tipo(MCuenta cuenta) {
        return cuenta instanceof MCuentaCorriente ? TIPO_CORRIENTE : TIPO_AHORRO;
    }
}
