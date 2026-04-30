package bankingsystem.view;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.CuentaAhorros;
import bankingsystem.services.CuentaAhorrosServices;
import bankingsystem.utils.PersonFormValidation;

public class CuentaAhorrosView {

    private final CuentaAhorrosServices service;

    public CuentaAhorrosView(CuentaAhorrosServices service) {
        this.service = service;
    }

    public void ConsultarEstado(CuentaAhorros cuenta) {
        if (cuenta != null) {
            System.out.println("Número: " + cuenta.getNumeroCuenta()); // No uses la variable directa, usa el getter
            System.out.println("Saldo Actual: $" + cuenta.getSaldo());
        } else {
            System.out.println("❌ No se encontró ninguna cuenta asociada.");
        }
    }

    public void realizarConsignacion(String username) {
        double monto = PersonFormValidation.validateDouble("Ingrese el monto a consignar: ");
        try {
            service.consignar(username, monto);
            System.out.println("✅ Consignación exitosa en cuenta de ahorros.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void realizarRetiro(String username) {
        double monto = PersonFormValidation.validateDouble("Ingrese el monto a retirar: ");
        try {
            service.retirar(username, monto);
            System.out.println("✅ Retiro exitoso.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    public void consultarEstado(String username) {
        try {
            CuentaAhorros cuenta = (CuentaAhorros) service.consultarEstado(username);
            if (cuenta != null) {
                System.out.println("Número: " + cuenta.getNumeroCuenta());
                System.out.println("Saldo Actual: $" + cuenta.getSaldo());
            } else {
                System.out.println("❌ No se encontró ninguna cuenta de ahorros asociada.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error al consultar estado: " + e.getMessage());
        }

    }
}