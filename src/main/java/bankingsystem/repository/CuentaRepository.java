package bankingsystem.repository;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.enums.TypoCuenta;
import java.util.ArrayList;
import java.util.List;

public class CuentaRepository {
    private final List<Cuenta> cuentas = new ArrayList<>();

    public Cuenta saveCuenta(Cuenta cuenta) {
        cuentas.add(cuenta);
        return cuenta;
    }

    public List<Cuenta> findAllCuentas() {
        return cuentas;
    }

    public Cuenta findByNumeroCuenta(String numeroCuenta) {
        for (Cuenta cuenta : cuentas) {
            if (cuenta.getNumeroCuenta() != null && cuenta.getNumeroCuenta().equalsIgnoreCase(numeroCuenta)) {
                return cuenta;
            }
        }
        return null;
    }

    public List<Cuenta> findByPropietario(String propietario) {
        List<Cuenta> resultado = new ArrayList<>();
        for (Cuenta cuenta : cuentas) {
            if (cuenta.getPropietario() != null && cuenta.getPropietario().equalsIgnoreCase(propietario)) {
                resultado.add(cuenta);
            }
        }
        return resultado;
    }

    public Cuenta findByPropietarioAndTipo(String propietario, TypoCuenta tipo) {
        for (Cuenta cuenta : cuentas) {
            if (cuenta.getPropietario() != null
                    && cuenta.getPropietario().equalsIgnoreCase(propietario)
                    && cuenta.getTipo() == tipo) {
                return cuenta;
            }
        }
        return null;
    }

    // Compatibilidad temporal con código legado.
    public Cuenta findCuentaBynumCuenta(String identificador) {
        return findByNumeroCuenta(identificador);
    }
}