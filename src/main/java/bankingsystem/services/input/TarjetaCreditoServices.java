package bankingsystem.services.input;

import bankingsystem.domain.TarjetaCredito;

public interface TarjetaCreditoServices {
    void realizarCompra(String username, double monto, int cuotas);
    void pagarCuota(String username, double monto);
    TarjetaCredito buscarTarjeta(String username);
}
