package bankingsystem.repository;


import bankingsystem.domain.CuentaAhorros;
import bankingsystem.domain.CuentaCorriente;

import java.util.ArrayList;

public class CuentaCorrienteRepository {
    private final ArrayList<CuentaCorriente> cuentaCorrientes = new ArrayList<>();

    public void saveCuentaC(CuentaCorriente cuentaCorriente) {
        cuentaCorrientes.add(cuentaCorriente);
    }
    public CuentaCorriente findCuentaCorriente() {
        for (CuentaCorriente c : cuentaCorrientes) {
        if (c.equals(cuentaCorrientes)) {}
                return c;
            }
        return null;
    }
    public CuentaCorriente findCuentaCorrienteByNumCuenta(String numCuenta) {
        for (CuentaCorriente cuenta : cuentaCorrientes) {
            if (cuenta.getNumeroCuenta().equals(numCuenta)) {
                return cuenta;
            }
        }
        return null;
    }
    public CuentaCorriente findbypropietario(String propietario) {
        for (CuentaCorriente cuenta : cuentaCorrientes) {
            if (cuenta.getPropietario().equals(propietario)) {
                return cuenta;
            }
        }
        return null;
    }

}