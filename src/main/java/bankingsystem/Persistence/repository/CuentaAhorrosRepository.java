package bankingsystem.Persistence.repository;

import bankingsystem.domain.CuentaAhorros;
import bankingsystem.services.outputport.CuentaAhorrosPersistencePort;

import java.util.ArrayList;
import java.util.List;

public class CuentaAhorrosRepository implements CuentaAhorrosPersistencePort {

    private final List<CuentaAhorros> cuentasAhorros = new ArrayList<>();

    @Override
    public void saveCuentaAhorros(CuentaAhorros cuenta) {
        cuentasAhorros.add(cuenta);
    }

    @Override
    public int countCuentas() {
        return cuentasAhorros.size();
    }

    @Override
    public CuentaAhorros findByPropietario(String username) {
        return cuentasAhorros.stream()
                .filter(c -> c.getPropietario().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public void saveCuentaA(CuentaAhorros cuentaahorros) {
        cuentasAhorros.add(cuentaahorros);
    }

    public CuentaAhorros findCuentaAhorrosByNumCuenta(String numCuenta) {
        for (CuentaAhorros cuenta : cuentasAhorros) {
            if (cuenta.getNumeroCuenta().equals(numCuenta)) {
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
