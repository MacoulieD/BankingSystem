package bankingsystem.view;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.TarjetaCredito;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.services.CuentaServices;
import bankingsystem.services.TarjetaCreditoServices;
import bankingsystem.utils.PersonFormValidation; // Asegúrate de tener este import para capturar montos


public class CuentaView {
    private final CuentaServices cuentaServices;
    private final TarjetaCreditoServices tarjetaCreditoServices;

    public CuentaView(CuentaServices cuentaServices, TarjetaCreditoServices tarjetaCreditoServices) {
        this.cuentaServices = cuentaServices;
        this.tarjetaCreditoServices = tarjetaCreditoServices;
    }

    public void crearCuenta(String username, double saldoInicial, int tipoSeleccionado) {
        try {
            cuentaServices.crearCuenta(username, saldoInicial, TypoCuenta.desdeId(tipoSeleccionado));
        } catch (Exception e) {
            System.out.println("❌ Error al crear la cuenta: " + e.getMessage());
        }
    }

    public void consultarEstado(String username, TypoCuenta tipo) {
        Cuenta cuenta = cuentaServices.obtenerCuenta(username, tipo);
        if (cuenta != null) {
            System.out.println("\n--- ESTADO DE CUENTA ---");
            System.out.println("Número: " + cuenta.getNumeroCuenta());


            System.out.printf("Saldo Actual: $%.2f%n", cuenta.getSaldo());

        } else {
            System.out.println("⚠️ No se encontró la cuenta.");
        }
    }

    public void consultarEstadoTarjeta(String username) {
        TarjetaCredito tarjeta = tarjetaCreditoServices.buscarTarjeta(username);
        if (tarjeta == null) {
            System.out.println("⚠️ No tienes una tarjeta de crédito activa.");
            return;
        }

        System.out.println("\n--- ESTADO TARJETA DE CRÉDITO ---");
        System.out.println("Número: " + tarjeta.getNumeroCuenta());
        System.out.printf("Deuda Actual: $%,.2f%n", tarjeta.getSaldo());
        System.out.printf("Cupo Total: $%,.2f%n", tarjeta.getCupoTotal());
        System.out.printf("Cupo Disponible: $%,.2f%n", tarjeta.getCupoDisponible());
    }


    public void realizarConsignacion(String username, TypoCuenta tipo) {
        try {
            if (tipo == TypoCuenta.TARJETA_CREDITO) {
                double montoCompra = PersonFormValidation.validateDouble("Valor de la compra: ");
                int cuotas = PersonFormValidation.validateInt("Número de cuotas: ");
                if (cuotas <= 0) {
                    System.out.println("❌ El número de cuotas debe ser mayor a cero.");
                    return;
                }

                tarjetaCreditoServices.realizarCompra(username, montoCompra, cuotas);
                return;
            }

            double monto = PersonFormValidation.validateDouble("Monto a consignar: ");


            cuentaServices.consignar(username, tipo, monto);


            System.out.printf("✅ Consignación de $%,.2f realizada con éxito.%n", monto);

        } catch (Exception e) {

            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    public void realizarRetiro(String username, TypoCuenta tipo) {
        try {
            if (tipo == TypoCuenta.TARJETA_CREDITO) {
                double montoPago = PersonFormValidation.validateDouble("Monto a pagar de la deuda: ");
                tarjetaCreditoServices.pagarCuota(username, montoPago);
                return;
            }

            double monto = PersonFormValidation.validateDouble("Monto a retirar: ");
            cuentaServices.retirar(username, tipo, monto);
            System.out.println("✅ Retiro exitoso.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    public void verMovimientos(String username, TypoCuenta tipo) {
        Cuenta cuenta = cuentaServices.obtenerCuenta(username, tipo);
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