package bankingsystem.view;

import bankingsystem.domain.CuentaCorriente;
import bankingsystem.services.CuentaCorrienteServices;
import bankingsystem.utils.CuentaCorrienteFromValidation;


public class CuentaCorrienteView {

    private final CuentaCorrienteServices service;

    public CuentaCorrienteView(CuentaCorrienteServices service) {
        this.service = service;
    }


    public void realizarRetiro(String username) {
        try {
            System.out.println("\n--- RETIRO CUENTA CORRIENTE ---");
            double monto = CuentaCorrienteFromValidation.validateMonto(
                    "Ingrese el monto a retirar (se aplicará sobregiro si es necesario): "
            );

            service.retirarConSobregiro(username, monto);
            System.out.println("✅ Operación realizada con éxito.");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    public void mostrarInfo(String identificador) {
        // Buscamos la cuenta a través del servicio
        CuentaCorriente cc = service.buscarCuenta(identificador);

        if (cc != null) {
            System.out.println("\n========================================");
            System.out.println("     📎 DETALLE CUENTA CORRIENTE");
            System.out.println("========================================");
            System.out.println("Propietario:     " + cc.getPropietario());
            System.out.println("Saldo actual:    $" + String.format("%.2f", cc.getSaldo()));
            System.out.println("Cupo sobregiro:  $" + String.format("%.2f", cc.getCupoSobregiro()));
            System.out.println("----------------------------------------");
            System.out.println("LÍMITE TOTAL:    $" + String.format("%.2f", cc.getCupoSobregiro()));
            System.out.println("========================================\n");
        } else {
            System.out.println("⚠️ No se encontró información para: " + identificador);
        }
    }
}