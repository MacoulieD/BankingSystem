package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.enums.TypoCuenta;

import java.util.List;

public interface CuentaServices {
    void crearCuenta(String username, double saldoInicial, TypoCuenta tipo);

    Cuenta obtenerCuentaPorTipo(String username, TypoCuenta tipo);

    void consignar(String username, TypoCuenta tipo, double monto);
    void retirar(String username, TypoCuenta tipo, double monto);
    void transferirEntrePropias(String username, TypoCuenta tipoOrigen, TypoCuenta tipoDestino, double monto);
    void transferirATercero(String usernameOrigen, TypoCuenta tipoOrigen, String numeroCuentaDestino, double monto);
    Cuenta obtenerCuenta(String username, TypoCuenta tipo);
    List<Cuenta> listarTodasLasCuentas();

}
