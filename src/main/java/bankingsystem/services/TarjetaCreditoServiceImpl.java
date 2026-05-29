package bankingsystem.services;

import bankingsystem.domain.Movimiento;
import bankingsystem.domain.TarjetaCredito;
import bankingsystem.domain.enums.TipoMovimiento;
import bankingsystem.Persistence.repository.MovimientoRepository;
import bankingsystem.services.input.TarjetaCreditoServices;
import bankingsystem.services.outputport.TarjetaCreditoPersistencePort; // ✅ Puerto importado correctamente

import java.util.List;

public class TarjetaCreditoServiceImpl implements TarjetaCreditoServices {

    // ✅ CORREGIDO: Ahora el atributo apunta directamente a la interfaz del puerto
    private final TarjetaCreditoPersistencePort tarjetaRepo;
    private final MovimientoRepository movimientoRepo;

    // ✅ CORREGIDO: Constructor alineado con la Arquitectura Hexagonal
    public TarjetaCreditoServiceImpl(TarjetaCreditoPersistencePort tarjetaRepo, MovimientoRepository movimientoRepo) {
        this.tarjetaRepo = tarjetaRepo;
        this.movimientoRepo = movimientoRepo;
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

        if (tc.getSaldo() < monto) { // Nota: Recuerda que 'getSaldo()' en tu dominio maneja el cupo disponible libre en memoria
            throw new RuntimeException("Cupo insuficiente. Disponible: $" + tc.getSaldo());
        }

        double valorCuota = tc.calcularCuotaMensual(monto, cuotas);
        String obs = tc.getObservacionInteres(cuotas);

        // Al comprar, disminuye el saldo disponible en memoria
        tc.setSaldo(tc.getSaldo() - monto);

        Movimiento movCompra = new Movimiento(
                tc.getMovimientos().size() + 1,
                TipoMovimiento.COMPRA_TC,
                monto,
                tc.getSaldo(),
                String.format("Compra: +$%,.2f (%d cuotas de $%,.2f - %s)", monto, cuotas, valorCuota, obs)
        );
        tc.getMovimientos().add(movCompra);
        movimientoRepo.save(movCompra);

        // ✅ NUEVO: Sincronizamos y persistimos el cambio de estado de la tarjeta en la base de datos
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

        // Calculamos la deuda real basándonos en la firma de tu entidad
        double cupoTotal = tc.getCupoTotal(tc.getSaldo());
        double deudaActual = cupoTotal - tc.getSaldo();

        if (monto > deudaActual) {
            throw new RuntimeException("El pago excede la deuda actual ($" + deudaActual + ")");
        }

        // Al pagar, recuperamos espacio en el saldo disponible en memoria
        tc.setSaldo(tc.getSaldo() + monto);

        Movimiento movPago = new Movimiento(
                tc.getMovimientos().size() + 1,
                TipoMovimiento.PAGO_TC,
                monto,
                tc.getSaldo(),
                String.format("Pago realizado: -$%,.2f", monto)
        );
        tc.getMovimientos().add(movPago);
        movimientoRepo.save(movPago);

        // ✅ NUEVO: Guardamos el estado actualizado en MySQL
        tarjetaRepo.saveTarjeta(tc);
    }

    @Override
    public TarjetaCredito buscarTarjeta(String username) {
        // ✅ CORREGIDO: Cambiado el filtro manual stream de memoria por consulta directa optimizada a MySQL
        List<TarjetaCredito> tarjetas = tarjetaRepo.findByPropietario(username);
        return (tarjetas != null && !tarjetas.isEmpty()) ? tarjetas.get(0) : null;
    }
}