package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.CuentaAhorros;

public interface CuentaAhorrosServices {
    // Métodos abstractos (sin llaves, sin static)
    void crearCuenta(String username, double saldoInicial);
    void consignar(String username, double monto);
    void retirar(String username, double monto);
    Cuenta consultarEstado(String username);
    CuentaAhorros buscarCuenta(String numeroCuenta);
    CuentaAhorros obtenerCuenta(String username);
}