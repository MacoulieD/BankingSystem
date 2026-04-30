package bankingsystem.repository;

import bankingsystem.domain.Cuenta;
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

    public Cuenta findCuentaBynumCuenta(String identificador) {
        for (Cuenta cuenta : cuentas) {
            if (cuenta.getNumeroCuenta() != null && cuenta.getNumeroCuenta().equalsIgnoreCase(identificador)) {
                return cuenta;
            }
            if (cuenta.getPropietario() != null && cuenta.getPropietario().equalsIgnoreCase(identificador)) {
                return cuenta;
            }
        }
        return null;
    }
}