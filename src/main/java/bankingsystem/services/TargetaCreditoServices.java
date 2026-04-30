package bankingsystem.services;

import bankingsystem.domain.TarjetaCredito;


public interface TargetaCreditoServices {

    void realizarCompra(String numeroTarjeta, double monto, int cuotas);


    void realizarPago(String numeroTarjeta, double monto);


    void realizarAvance(String numeroTarjeta, double monto);


    TarjetaCredito buscarPorNumero(String numeroTarjeta);

    boolean tieneCupoDisponible(String numeroTarjeta, double monto);
}