package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.CuentaAhorros;
import bankingsystem.domain.Movimiento;
import bankingsystem.domain.enums.TipoMovimiento;
import bankingsystem.Persistence.repository.CuentaAhorrosRepository;
import bankingsystem.Persistence.repository.MovimientoRepository;


public class CuentaAhorrosServiceImpl implements CuentaAhorrosServices {

    private final CuentaAhorrosRepository cuentaRepository;
    private final MovimientoRepository movimientoRepo;

    public CuentaAhorrosServiceImpl(CuentaAhorrosRepository cuentaRepository, MovimientoRepository movimientoRepo) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepo = movimientoRepo;
    }
    @Override
    public void crearCuenta(String username, double saldoInicial) {

        String numCuenta = "CA-" + Math.abs(username.hashCode());

        CuentaAhorros nuevaCuenta = new CuentaAhorros(numCuenta, saldoInicial, username);

        cuentaRepository.saveCuentaA(nuevaCuenta);
    }

    @Override
    public void consignar(String username, double monto) {

        Cuenta cuentaBase = obtenerCuenta(username);
        CuentaAhorros cuenta = null;

        if (cuentaBase instanceof CuentaAhorros) {
            cuenta = (CuentaAhorros) cuentaBase;
        }


        if (cuenta == null) {
            String numCuenta = "CA-" + Math.abs(username.hashCode());
            cuenta = new CuentaAhorros(numCuenta, 0, username);
            cuentaRepository.saveCuentaA(cuenta); // Guardamos en el repositorio real
        }

        if (monto > 0) {
            cuenta.setSaldo(cuenta.getSaldo() + monto);
            Movimiento movCA = new Movimiento(cuenta.getMovimientos().size() + 1, TipoMovimiento.CONSIGNACION, monto, cuenta.getSaldo(), String.format("Consignación: +$%.2f", monto));
            cuenta.getMovimientos().add(movCA);
            movimientoRepo.save(movCA);
        } else {
            throw new RuntimeException("❌ El monto a consignar debe ser positivo.");
        }
    }

    @Override
    public void retirar(String username, double monto) {
        Cuenta cuentaBase = obtenerCuenta(username);

        if (!(cuentaBase instanceof CuentaAhorros)) {
            throw new RuntimeException("❌ No se encontró una cuenta de ahorros para: " + username);
        }

        CuentaAhorros cuenta = (CuentaAhorros) cuentaBase;

        if (monto <= 0) {
            throw new RuntimeException("❌ El monto debe ser mayor a cero.");
        }

        if (monto > cuenta.getSaldo()) {
            throw new RuntimeException("🚫 Saldo insuficiente. Saldo actual: $" + cuenta.getSaldo());
        }

        cuenta.setSaldo(cuenta.getSaldo() - monto);
        Movimiento movRA = new Movimiento(cuenta.getMovimientos().size() + 1, TipoMovimiento.RETIRO, monto, cuenta.getSaldo(), String.format("Retiro: -$%.2f", monto));
        cuenta.getMovimientos().add(movRA);
        movimientoRepo.save(movRA);
    }

    @Override
    public Cuenta consultarEstado(String username) {
        return cuentaRepository.fidByPropietario(username);
    }

    @Override
    public CuentaAhorros buscarCuenta(String numeroCuenta) {
        return cuentaRepository.findCuentaAhorrosByNumCuenta(numeroCuenta);
    }

    @Override
    public CuentaAhorros obtenerCuenta(String username) {
        return cuentaRepository.fidByPropietario(username);
    }

}