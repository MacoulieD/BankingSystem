package bankingsystem.services.outputport;

import bankingsystem.domain.CuentaCorriente;

public interface CuentaCorrientePersistencePort {
    CuentaCorriente saveCuentaC(CuentaCorriente cuenta);
    CuentaCorriente findbypropietario(String username);
    int countCuentas(); // Contrato que debe cumplir el adaptador
}