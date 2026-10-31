package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Excepcion.ExcepcionNegocio;
import org.springframework.http.HttpStatus;

import java.util.Locale;

// Validaciones de montos compartidas por las operaciones (SCuenta,
// SMovimiento, STarjetaCredito). Los montos llegan como Double para poder
// rechazar decimales (si llegaran como Long, Jackson cortaria 10.5 a 10).
public final class Validaciones {

    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    private Validaciones() {
    }

    // monto de una operacion: entero y mayor que 0 (mismo mensaje del frontend)
    public static double montoEntero(Double monto) throws ExcepcionNegocio {
        if (monto == null || monto <= 0 || monto != Math.floor(monto) || monto > 999_999_999_999_999.0) {
            throw new ExcepcionNegocio(HttpStatus.BAD_REQUEST, "Ingresa un monto entero mayor que cero.");
        }
        return monto;
    }

    // saldo inicial de una cuenta: entero mayor o igual a 0
    public static double enteroNoNegativo(Double valor, String mensaje) throws ExcepcionNegocio {
        if (valor == null || valor < 0 || valor != Math.floor(valor) || valor > 999_999_999_999_999.0) {
            throw new ExcepcionNegocio(HttpStatus.BAD_REQUEST, mensaje);
        }
        return valor;
    }

    // 1500000 -> "$ 1.500.000" (como formatMoney del frontend)
    public static String pesos(double valor) {
        return String.format(COLOMBIA, "$ %,.0f", valor);
    }

    // deja solo los digitos ("1234-5678-90" -> "1234567890"); null -> ""
    public static String digitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }
}
