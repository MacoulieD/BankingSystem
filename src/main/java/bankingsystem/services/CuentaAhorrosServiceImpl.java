package bankingsystem.services;

import bankingsystem.domain.CuentaAhorros;
import bankingsystem.repository.CuentaAhorrosRepository;

public class CuentaAhorrosServiceImpl implements CuentaAhorrosServices {
    private final CuentaAhorrosRepository cuentaRepository;

    public CuentaAhorrosServiceImpl(CuentaAhorrosRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public void consignar(String numeroCuenta, double monto) {
        CuentaAhorros cuenta = cuentaRepository.findByNumero(numeroCuenta);
        if (cuenta != null && monto > 0) {
            cuenta.setSaldo(cuenta.getSaldo() + monto);
            cuenta.getMovimientos().add("Consignación: +$" + monto);
        }
    }

    @Override
    public void retirar(String numeroCuenta, double monto) {
        CuentaAhorros cuenta = cuentaRepository.findByNumero(numeroCuenta);

        if (cuenta == null) throw new RuntimeException("Cuenta no encontrada.");
        if (monto > cuenta.getSaldo()) throw new RuntimeException("Saldo insuficiente.");

        // Lógica de rendimiento: se suma el 1.5% del monto antes de retirar
        double interes = cuenta.calcularInteresRendimiento(monto);
        double nuevoSaldo = cuenta.getSaldo() - monto + interes;

        cuenta.setSaldo(nuevoSaldo);
        cuenta.getMovimientos().add(String.format("Retiro: -$%.2f | Rendimiento: +$%.2f", monto, interes));
    }

    @Override
    public CuentaAhorros buscarCuenta(String numeroCuenta) {
        return cuentaRepository.findByNumero(numeroCuenta);
    }
}