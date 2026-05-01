package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.enums.TypoCuenta;

import java.util.List;

public interface CuentaServices {
    void crearCuenta(String username, double saldoInicial, TypoCuenta tipo);

    Cuenta obtenerCuentaPorTipo(String username, TypoCuenta tipo);

    void consignar(String username, TypoCuenta tipo, double monto);
    void retirar(String username, TypoCuenta tipo, double monto);
    Cuenta obtenerCuenta(String username, TypoCuenta tipo);
    List<Cuenta> listarTodasLasCuentas();

}
