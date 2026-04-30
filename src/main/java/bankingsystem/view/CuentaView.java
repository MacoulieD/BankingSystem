package bankingsystem.view;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.services.CuentaServices;
import bankingsystem.utils.PersonFormValidation; // Asegúrate de tener este import para capturar montos


public class CuentaView {
    private final CuentaServices cuentaServices;

    public CuentaView(CuentaServices cuentaServices) {
        this.cuentaServices = cuentaServices;
    }

    public void crearCuenta(String username, double saldoInicial, int tipoSeleccionado) {
        try {
            cuentaServices.crearCuenta(username, saldoInicial, TypoCuenta.desdeId(tipoSeleccionado));
        } catch (Exception e) {
            System.out.println("❌ Error al crear la cuenta: " + e.getMessage());
        }
    }

    public void consultarEstado(String username, String ahorros) {
        Cuenta cuenta = cuentaServices.obtenerCuenta(username);
        if (cuenta != null) {
            System.out.println("\n--- ESTADO DE CUENTA ---");
            System.out.println("Número: " + cuenta.getNumeroCuenta());


            System.out.printf("Saldo Actual: $%.2f%n", cuenta.getSaldo());

        } else {
            System.out.println("⚠️ No se encontró la cuenta.");
        }
    }


    public void realizarConsignacion(String username, String ahorros) { // Quitamos el String innecesario
        try {

            double monto = PersonFormValidation.validateDouble("Monto a consignar: ");


            cuentaServices.consignar(username, monto);


            System.out.printf("✅ Consignación de $%,.2f realizada con éxito.%n", monto);

        } catch (Exception e) {

            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    public void realizarRetiro(String username, String ahorros) {
        try {
            double monto = PersonFormValidation.validateDouble("Monto a retirar: ");
            cuentaServices.retirar(username, monto);
            System.out.println("✅ Retiro exitoso.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    public void verMovimientos(String username, String ahorros) {
        Cuenta cuenta = cuentaServices.obtenerCuenta(username);
        if (cuenta != null && !cuenta.getMovimientos().isEmpty()) {
            System.out.println("\n--- HISTORIAL DE MOVIMIENTOS ---");
            for (String mov : cuenta.getMovimientos()) {
                System.out.println("- " + mov);
            }
        } else {
            System.out.println("ℹ️ No hay movimientos registrados.");
        }
    }
}