package bankingsystem.services;

import bankingsystem.domain.TarjetaCredito;
import bankingsystem.domain.enums.TypoCuenta;

public interface TarjetaCreditoServices {
    void realizarCompra(String username, double monto, int cuotas);
    void pagarCuota(String username, double monto);
    TarjetaCredito buscarTarjeta(String username);
}
