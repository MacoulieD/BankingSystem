package bankingsystem.services;

import bankingsystem.domain.CuentaCorriente;

public interface CuentaCorrienteService {
    void consignar(String numero, double monto);
    void retirarConSobregiro(String numero, double monto);
    CuentaCorriente buscarCuenta(String numero);
}