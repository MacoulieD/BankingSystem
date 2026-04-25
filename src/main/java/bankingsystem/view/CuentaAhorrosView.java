package bankingsystem.view;

import bankingsystem.domain.CuentaAhorros;
import bankingsystem.services.CuentaAhorrosServices;
import bankingsystem.utils.CuentaAhorrosFormValidation;
import bankingsystem.utils.PersonFormValidation;

public class CuentaAhorrosView {
    private final CuentaAhorrosServices cuentaService;

    public CuentaAhorrosView(CuentaAhorrosServices cuentaService) {
        this.cuentaService = cuentaService;
    }

    public void realizarRetiro(String numeroCuenta) {
        try {
            double monto = CuentaAhorrosFormValidation.validateMonto("Monto a retirar: ");
            cuentaService.retirar(numeroCuenta, monto);
            System.out.println("✅ Retiro exitoso. Rendimiento del 1.5% aplicado.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void consultarEstado(String numeroCuenta) {
        CuentaAhorros cuenta = cuentaService.buscarCuenta(numeroCuenta);
        if (cuenta != null) {
            System.out.println("\n--- ESTADO DE CUENTA ---");
            System.out.println("Número: " + cuenta.getNumeroCuenta());
            System.out.println("Saldo actual: $" + cuenta.getSaldo());
            System.out.println("Movimientos:");
            cuenta.getMovimientos().forEach(m -> System.out.println(" - " + m));
        }
    }
}