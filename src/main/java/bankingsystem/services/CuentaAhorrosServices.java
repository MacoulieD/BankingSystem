package bankingsystem.services;

import bankingsystem.domain.CuentaAhorros;

public interface CuentaAhorrosServices {
    void consignar(String numeroCuenta, double monto);
    void retirar(String numeroCuenta, double monto);
    CuentaAhorros buscarCuenta(String numeroCuenta);
}