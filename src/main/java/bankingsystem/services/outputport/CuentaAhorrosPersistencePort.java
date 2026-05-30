package bankingsystem.services.outputport;

import bankingsystem.domain.CuentaAhorros;

public interface CuentaAhorrosPersistencePort {
    CuentaAhorros saveCuentaAhorros(CuentaAhorros cuenta);
    CuentaAhorros findByPropietario(String username);
    int countCuentas();
}
