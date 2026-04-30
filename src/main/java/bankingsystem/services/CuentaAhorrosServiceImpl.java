package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.CuentaAhorros;
import bankingsystem.repository.CuentaAhorrosRepository;


public class CuentaAhorrosServiceImpl implements CuentaAhorrosServices {

    private final CuentaAhorrosRepository cuentaRepository;

    public CuentaAhorrosServiceImpl(CuentaAhorrosRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
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
            cuenta.getMovimientos().add(String.format("Consignación: +$%.2f", monto));
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
        cuenta.getMovimientos().add(String.format("Retiro: -$%.2f", monto));
    }

    @Override
    public Cuenta consultarEstado(String username) {
        return null;
    }

    @Override
    public CuentaAhorros buscarCuenta(String numeroCuenta) {

        return cuentaRepository.findCuentaAhorros();
    }

    @Override
    public CuentaAhorros obtenerCuenta(String username) {
        return null;
    }

}