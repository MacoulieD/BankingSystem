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
            throw new RuntimeException("❌ No tienes una Tarjeta de Crédito activa.");
        }


        if (tc.getCupoDisponible() < monto) {
            throw new RuntimeException("❌ Cupo insuficiente. Disponible: $" + tc.getCupoDisponible());
        }


        double valorCuota = tc.calcularCuotaMensual(monto, cuotas);
        String obs = tc.getObservacionInteres(cuotas);

        tc.setSaldo(tc.getSaldo() + monto);


        tc.getMovimientos().add(String.format("Compra: +$%,.2f (%d cuotas de $%,.2f - %s)",
                monto, cuotas, valorCuota, obs));

        System.out.printf("✅ Compra exitosa. Cuota mensual: $%,.2f (%s)%n", valorCuota, obs);
    }

    @Override
    public void pagarCuota(String username, double monto) {
        // Buscamos la tarjeta
        TarjetaCredito tc = tarjetaRepo.findAllTarjetas().stream()
                .filter(t -> t.getPropietario().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);

        if (tc == null) {
            throw new RuntimeException("❌ No se encontró la tarjeta.");
        }

        if (monto > tc.getSaldo()) {
            throw new RuntimeException("❌ El pago excede la deuda actual ($" + tc.getSaldo() + ")");
        }


        tc.setSaldo(tc.getSaldo() - monto);
        tc.getMovimientos().add(String.format("Pago realizado: -$%,.2f", monto));

        System.out.println("✅ Pago aplicado con éxito. Cupo liberado.");
    }

    @Override
    public TarjetaCredito buscarTarjeta(String username) {
        return tarjetaRepo.findAllTarjetas().stream()
                .filter(t -> t.getPropietario().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }
}