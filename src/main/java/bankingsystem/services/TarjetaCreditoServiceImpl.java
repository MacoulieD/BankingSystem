package bankingsystem.services;


import bankingsystem.domain.TarjetaCredito;
import bankingsystem.repository.TarjetaCreditoRepository;

public class TarjetaCreditoServiceImpl implements TarjetaCreditoServices {

    private final TarjetaCreditoRepository tarjetaRepo;

    public TarjetaCreditoServiceImpl(TarjetaCreditoRepository tarjetaRepo) {
        this.tarjetaRepo = tarjetaRepo;
    }

    @Override
    public void realizarCompra(String username, double monto, int cuotas) {
        TarjetaCredito tc = tarjetaRepo.findAllTarjetas().stream()
                .filter(t -> t.getPropietario().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);

        if (tc == null) {
            throw new RuntimeException("No tienes una tarjeta de crédito activa.");
        }

        if (monto <= 0) {
            throw new RuntimeException("El valor de la compra debe ser mayor a cero.");
        }

        if (cuotas <= 0) {
            throw new RuntimeException("El número de cuotas debe ser mayor a cero.");
        }


        if (tc.getCupoDisponible() < monto) {
            throw new RuntimeException("Cupo insuficiente. Disponible: $" + tc.getCupoDisponible());
        }


        double valorCuota = tc.calcularCuotaMensual(monto, cuotas);
        String obs = tc.getObservacionInteres(cuotas);

        tc.setSaldo(tc.getSaldo() + monto);


        tc.getMovimientos().add(String.format("Compra: +$%,.2f (%d cuotas de $%,.2f - %s)",
                monto, cuotas, valorCuota, obs));
    }

    @Override
    public void pagarCuota(String username, double monto) {
        // Buscamos la tarjeta
        TarjetaCredito tc = tarjetaRepo.findAllTarjetas().stream()
                .filter(t -> t.getPropietario().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);

        if (tc == null) {
            throw new RuntimeException("No se encontró la tarjeta.");
        }

        if (monto <= 0) {
            throw new RuntimeException("El monto de pago debe ser mayor a cero.");
        }

        if (monto > tc.getSaldo()) {
            throw new RuntimeException("El pago excede la deuda actual ($" + tc.getSaldo() + ")");
        }


        tc.setSaldo(tc.getSaldo() - monto);
        tc.getMovimientos().add(String.format("Pago realizado: -$%,.2f", monto));
    }

    @Override
    public TarjetaCredito buscarTarjeta(String username) {
        return tarjetaRepo.findAllTarjetas().stream()
                .filter(t -> t.getPropietario().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }
}