package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.CuentaCorriente;

public interface CuentaCorrienteServices {
    void crearCuenta(String username, double saldoInicial);
    void consignar(String username, double monto);
    void retirarConSobregiro(String username, double monto);
    Cuenta consultarCuenta(String username);
    Cuenta obtenerCuenta(String username);
    CuentaCorriente buscarCuenta(String numero);
}