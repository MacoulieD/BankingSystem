package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.enums.TypoCuenta;

import java.util.List;

public interface CuentaServices {
    void crearCuenta(String username, double saldoInicial, TypoCuenta tipo);;

    Cuenta obtenerCuentaPorTipo(String username, TypoCuenta tipo);

    void consignar(String username, double monto);
    void retirar(String username, double monto);
    Cuenta obtenerCuenta(String username);
    List<Cuenta> listarTodasLasCuentas();

}
