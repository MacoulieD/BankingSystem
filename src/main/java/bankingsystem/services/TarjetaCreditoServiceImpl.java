package bankingsystem.services;

import bankingsystem.domain.Movimiento;
import bankingsystem.domain.TarjetaCredito;
import bankingsystem.domain.enums.TipoMovimiento;
import bankingsystem.Persistence.repository.MovimientoRepository;
import bankingsystem.services.input.TarjetaCreditoServices;
import bankingsystem.services.outputport.MovimientoPersistencePort;
import bankingsystem.services.outputport.TarjetaCreditoPersistencePort;

import java.util.List;

public class TarjetaCreditoServiceImpl implements TarjetaCreditoServices {

    private final TarjetaCreditoPersistencePort tarjetaRepo;
    private final MovimientoRepository movimientoRepo;
    private final MovimientoPersistencePort movimientoPersistencePort;

    public TarjetaCreditoServiceImpl(TarjetaCreditoPersistencePort tarjetaRepo,
                                     MovimientoRepository movimientoRepo,
                                     MovimientoPersistencePort movimientoPersistencePort) {
        this.tarjetaRepo = tarjetaRepo;
        this.movimientoRepo = movimientoRepo;
        this.movimientoPersistencePort = movimientoPersistencePort;
    }

    @Override
    public void realizarCompra(String username, double monto, int cuotas) {
        // ✅ CORREGIDO: Buscamos directamente en MySQL por propietario usando el puerto
        List<TarjetaCredito> tarjetas = tarjetaRepo.findByPropietario(username);
        TarjetaCredito tc = (tarjetas != null && !tarjetas.isEmpty()) ? tarjetas.get(0) : null;

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

        // Al comprar, la deuda aumenta
        tc.setSaldo(tc.getSaldo() + monto);

        int nextIdCompra = movimientoPersistencePort.findByNumeroCuenta(tc.getNumeroCuenta()).size() + 1;
        Movimiento movCompra = new Movimiento(
                nextIdCompra,
                TipoMovimiento.COMPRA_TC,
                monto,
                tc.getCupoDisponible(),
                String.format("Compra: +$%,.2f (%d cuotas de $%,.2f - %s)", monto, cuotas, valorCuota, obs)
        );
        tc.getMovimientos().add(movCompra);
        movimientoRepo.save(movCompra);
        movimientoPersistencePort.saveMovimiento(tc.getNumeroCuenta(), movCompra);

        tarjetaRepo.saveTarjeta(tc);
    }

    @Override
    public void pagarCuota(String username, double monto) {
        // ✅ CORREGIDO: Buscamos directamente en MySQL usando el puerto
        List<TarjetaCredito> tarjetas = tarjetaRepo.findByPropietario(username);
        TarjetaCredito tc = (tarjetas != null && !tarjetas.isEmpty()) ? tarjetas.get(0) : null;

        if (tc == null) {
            throw new RuntimeException("No se encontró la tarjeta.");
        }

        if (monto <= 0) {
            throw new RuntimeException("El monto de pago debe ser mayor a cero.");
        }

        double deudaActual = tc.getSaldo();

        if (monto > deudaActual) {
            throw new RuntimeException("El pago excede la deuda actual ($" + deudaActual + ")");
        }

        // Al pagar, la deuda disminuye
        tc.setSaldo(tc.getSaldo() - monto);

        int nextIdPago = movimientoPersistencePort.findByNumeroCuenta(tc.getNumeroCuenta()).size() + 1;
        Movimiento movPago = new Movimiento(
                nextIdPago,
                TipoMovimiento.PAGO_TC,
                monto,
                tc.getCupoDisponible(),
                String.format("Pago realizado: -$%,.2f", monto)
        );
        tc.getMovimientos().add(movPago);
        movimientoRepo.save(movPago);
        movimientoPersistencePort.saveMovimiento(tc.getNumeroCuenta(), movPago);

        tarjetaRepo.saveTarjeta(tc);
    }

    @Override
    public TarjetaCredito buscarTarjeta(String username) {
        // ✅ CORREGIDO: Cambiado el filtro manual stream de memoria por consulta directa optimizada a MySQL
        List<TarjetaCredito> tarjetas = tarjetaRepo.findByPropietario(username);
        return (tarjetas != null && !tarjetas.isEmpty()) ? tarjetas.get(0) : null;
    }
}