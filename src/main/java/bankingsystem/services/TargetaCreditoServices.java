package bankingsystem.services;

import bankingsystem.domain.TarjetaCredito;

/**
 * Interfaz que define las operaciones de negocio para la Tarjeta de Crédito.
 * Incluye compras con cuotas, avances y pagos de deuda.
 */
public interface TargetaCreditoServices {

    /**
     * Procesa una compra aplicando intereses según el número de cuotas.
     */
    void realizarCompra(String numeroTarjeta, double monto, int cuotas);

    /**
     * Permite abonar a la deuda actual de la tarjeta.
     */
    void realizarPago(String numeroTarjeta, double monto);

    /**
     * Realiza un avance en efectivo (retiro) que se difiere a 1 cuota.
     */
    void realizarAvance(String numeroTarjeta, double monto);

    /**
     * Busca la información completa de la tarjeta por su número.
     */
    TarjetaCredito buscarPorNumero(String numeroTarjeta);

    /**
     * Verifica si el usuario tiene cupo suficiente antes de una operación.
     */
    boolean tieneCupoDisponible(String numeroTarjeta, double monto);
}