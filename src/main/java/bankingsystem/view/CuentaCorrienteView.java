package bankingsystem.view;

import bankingsystem.domain.CuentaCorriente;
import bankingsystem.services.CuentaCorrienteService;
import bankingsystem.utils.CuentaCorrienteFromValidation;
import bankingsystem.utils.CuentaCorrienteFromValidation;

public class CuentaCorrienteView {
    private final CuentaCorrienteService service;

    public CuentaCorrienteView(CuentaCorrienteService service) {
        this.service = service;
    }

    public void realizarRetiro(String numero) {
        try {
            double monto = CuentaCorrienteFromValidation.validateMonto("Monto a retirar (incluye sobregiro del 20%): ");
            service.retirarConSobregiro(numero, monto);
            System.out.println("✅ Operación realizada con éxito.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void mostrarInfo(String numero) {
        CuentaCorriente cc = service.buscarCuenta(numero);
        if (cc != null) {
            System.out.println("\n--- DETALLE CUENTA CORRIENTE ---");
            System.out.println("Saldo: $" + cc.getSaldo());
            System.out.println("Límite total con sobregiro: $" + cc.getLimiteDisponible());
        }
    }
}