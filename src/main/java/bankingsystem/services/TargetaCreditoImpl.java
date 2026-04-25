package bankingsystem.services;

import bankingsystem.repository.TargetaCreditoRepository;
import bankingsystem.domain.TarjetaCredito;

// 1. Corregido: Se elimina la clase envolvente 'TargetaCreditoImpl'
// 2. Corregido: El nombre del archivo debe coincidir con la clase pública
public class TarjetaCreditoServiceImpl implements TargetaCreditoServices {

    private final TargetaCreditoRepository repository;

    public TarjetaCreditoServiceImpl(TargetaCreditoServices repository) {
        this.repository = repository;
    }

    @Override
    public void realizarCompra(String numeroTarjeta, double monto, int cuotas) {
        TarjetaCredito tc = repository.findByNumero(numeroTarjeta);

        if (tc == null) {
            throw new RuntimeException("La tarjeta de crédito no existe.");
        }

        if (monto > tc.getCupoDisponible()) {
            throw new RuntimeException("Cupo insuficiente. Cupo disponible: $" + tc.getCupoDisponible());
        }

        // Aplicación de tasas según tu lógica del dominio
        double tasa = tc.calcularTasa(cuotas);

        // Interés simple sobre el monto total
        double intereses = monto * tasa * cuotas;
        double montoTotalConIntereses = monto + intereses;

        // Actualizamos la deuda
        tc.setDeudaActual(tc.getDeudaActual() + montoTotalConIntereses);

        // Registramos el movimiento en la lista del dominio
        String detalle = String.format("Compra: -$%.2f | Cuotas: %d | Tasa: %.1f%% | Interés total: $%.2f",
                monto, cuotas, (tasa * 100), intereses);
        tc.getMovimientos().add(detalle);
    }

    @Override
    public void realizarPago(String numeroTarjeta, double monto) {
        TarjetaCredito tc = repository.findByNumero(numeroTarjeta);

        if (tc == null) {
            throw new RuntimeException("Tarjeta no encontrada.");
        }

        if (monto > tc.getDeudaActual()) {
            throw new RuntimeException("El pago excede la deuda actual: $" + tc.getDeudaActual());
        }

        tc.setDeudaActual(tc.getDeudaActual() - monto);
        tc.getMovimientos().add("Pago de Tarjeta: +$" + monto + " | Deuda restante: $" + tc.getDeudaActual());
    }

    @Override
    public void realizarAvance(String numeroTarjeta, double monto) {
        // Un avance es una compra a 1 sola cuota (0% interés)
        realizarCompra(numeroTarjeta, monto, 1);
    }

    @Override
    public TarjetaCredito buscarPorNumero(String numeroTarjeta) {
        return repository.findByNumero(numeroTarjeta);
    }

    @Override
    public boolean tieneCupoDisponible(String numeroTarjeta, double monto) {
        TarjetaCredito tc = repository.findByNumero(numeroTarjeta);
        return tc != null && tc.getCupoDisponible() >= monto;
    }
}

