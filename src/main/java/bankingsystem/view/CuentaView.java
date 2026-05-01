package bankingsystem.view;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.TarjetaCredito;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.services.CuentaServices;
import bankingsystem.services.TarjetaCreditoServices;
import bankingsystem.utils.PersonFormValidation;


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
                tarjetaCreditoServices.realizarCompra(username, montoCompra, cuotas);

                TarjetaCredito tarjeta = tarjetaCreditoServices.buscarTarjeta(username);
                double valorCuota = tarjeta.calcularCuotaMensual(montoCompra, cuotas);
                String observacion = tarjeta.getObservacionInteres(cuotas);

                System.out.printf("✅ Compra registrada por $%,.2f.%n", montoCompra);
                System.out.printf("Cuota mensual: $%,.2f (%s)%n", valorCuota, observacion);
                System.out.printf("Deuda total: $%,.2f | Cupo disponible: $%,.2f%n",
                        tarjeta.getSaldo(),
                        tarjeta.getCupoDisponible());
                return;
            }

            double monto = PersonFormValidation.validateDouble("Monto a consignar: ");


            cuentaServices.consignar(username, tipo, monto);

            Cuenta cuentaActualizada = cuentaServices.obtenerCuenta(username, tipo);

            System.out.printf("✅ Consignación exitosa de $%,.2f.%n", monto);
            System.out.printf("Saldo actual de %s: $%,.2f%n", tipo, cuentaActualizada.getSaldo());

        } catch (Exception e) {

            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    public void realizarRetiro(String username, TypoCuenta tipo) {
        try {
            if (tipo == TypoCuenta.TARJETA_CREDITO) {
                double montoPago = PersonFormValidation.validateDouble("Monto a pagar de la deuda: ");
                tarjetaCreditoServices.pagarCuota(username, montoPago);

                TarjetaCredito tarjeta = tarjetaCreditoServices.buscarTarjeta(username);
                System.out.printf("✅ Pago aplicado por $%,.2f.%n", montoPago);
                System.out.printf("Deuda restante: $%,.2f | Cupo disponible: $%,.2f%n",
                        tarjeta.getSaldo(),
                        tarjeta.getCupoDisponible());
                return;
            }

            double monto = PersonFormValidation.validateDouble("Monto a retirar: ");
            cuentaServices.retirar(username, tipo, monto);

            Cuenta cuentaActualizada = cuentaServices.obtenerCuenta(username, tipo);
            System.out.printf("✅ Retiro exitoso de $%,.2f.%n", monto);
            System.out.printf("Saldo actual de %s: $%,.2f%n", tipo, cuentaActualizada.getSaldo());
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

    public void transferirDinero(String username) {
        try {
            System.out.println("\n--- INTERFAZ DE TRANSFERENCIAS ---");
            System.out.println("1. Entre mis cuentas");
            System.out.println("2. A otro usuario");

            int opcion = PersonFormValidation.validateInt("Seleccione tipo de transferencia: ");
            TypoCuenta tipoOrigen = solicitarTipoCuenta("Seleccione la cuenta origen (débito)");
            if (tipoOrigen == null) {
                System.out.println("❌ Tipo de cuenta origen no válido.");
                return;
            }

            double monto = PersonFormValidation.validateDouble("Monto a transferir: ");

            if (opcion == 1) {
                TypoCuenta tipoDestino = solicitarTipoCuenta("Seleccione la cuenta destino (crédito)");
                if (tipoDestino == null) {
                    System.out.println("❌ Tipo de cuenta destino no válido.");
                    return;
                }

                cuentaServices.transferirEntrePropias(username, tipoOrigen, tipoDestino, monto);

                Cuenta cuentaOrigen = cuentaServices.obtenerCuenta(username, tipoOrigen);
                Cuenta cuentaDestino = cuentaServices.obtenerCuenta(username, tipoDestino);
                System.out.printf("✅ Transferencia propia exitosa: $%,.2f de %s a %s.%n", monto, tipoOrigen, tipoDestino);
                System.out.printf("Saldo origen: $%,.2f | Saldo destino: $%,.2f%n",
                        cuentaOrigen.getSaldo(),
                        cuentaDestino.getSaldo());
                return;
            }

            if (opcion == 2) {
                String cuentaDestino = PersonFormValidation.validateString("Número de cuenta destino: ");
                cuentaServices.transferirATercero(username, tipoOrigen, cuentaDestino, monto);

                Cuenta cuentaOrigen = cuentaServices.obtenerCuenta(username, tipoOrigen);
                Cuenta destino = buscarCuentaPorNumero(cuentaDestino);
                String propietarioDestino = (destino != null) ? destino.getPropietario() : "usuario destino";
                double saldoDestino = (destino != null) ? destino.getSaldo() : 0;

                System.out.printf("✅ Transferencia a tercero exitosa: $%,.2f a la cuenta %s (%s).%n",
                        monto,
                        cuentaDestino,
                        propietarioDestino);
                System.out.printf("Saldo origen: $%,.2f%n", cuentaOrigen.getSaldo());
                if (destino != null) {
                    System.out.printf("Saldo destino: $%,.2f%n", saldoDestino);
                }
                return;
            }

            System.out.println("❌ Opción no válida.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private TypoCuenta solicitarTipoCuenta(String mensaje) {
        System.out.println(mensaje);
        System.out.println("1. Cuenta de Ahorros");
        System.out.println("2. Cuenta Corriente");
        int opcion = PersonFormValidation.validateInt("Seleccione: ");
        return switch (opcion) {
            case 1 -> TypoCuenta.AHORROS;
            case 2 -> TypoCuenta.CORRIENTE;
            default -> null;
        };
    }

    private Cuenta buscarCuentaPorNumero(String numeroCuenta) {
        return cuentaServices.listarTodasLasCuentas().stream()
                .filter(c -> c.getNumeroCuenta() != null && c.getNumeroCuenta().equalsIgnoreCase(numeroCuenta))
                .findFirst()
                .orElse(null);
    }
}