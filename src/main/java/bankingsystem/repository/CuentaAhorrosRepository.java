package bankingsystem.repository;

import bankingsystem.domain.CuentaAhorros;
import java.util.ArrayList;
import java.util.List;


public class CuentaAhorrosRepository {
    private final List<CuentaAhorros> cuentasAhorros = new ArrayList<>();

    public void saveCuentaA(CuentaAhorros cuentaahorros) {
        cuentasAhorros.add(cuentaahorros);
    }

    public CuentaAhorros findCuentaAhorros() {
        for (CuentaAhorros c : cuentasAhorros) {
            if (c.equals(cuentasAhorros)) {
                return c;
            }
        }
        return null;
    }
    public CuentaAhorros findAllAhorros(String username) {
        for (CuentaAhorros c : cuentasAhorros) {
            if (c.getPropietario().equalsIgnoreCase(username)) {}
        }
        return null;
    }

    public CuentaAhorros findCuentaAhorrosByNumCuenta(String numCuenta) {
        for (CuentaAhorros cuenta : cuentasAhorros) {
            if (cuenta.getNumeroCuenta().equals(numCuenta)) {
                return cuenta;
            }
        }
        return null;
    }
    public CuentaAhorros findCuentaAhorrosByTipo(String tipo) {
        for (CuentaAhorros cuenta : cuentasAhorros) {
            if (cuenta.getTypoCuenta().equalsIgnoreCase(tipo)) {
                return cuenta;
            }
        }
        return null;
    }
    public CuentaAhorros fidByPropietario(String username) {
        return cuentasAhorros.stream()
                .filter(c -> c.getPropietario().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

}
