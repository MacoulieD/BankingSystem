package bankingsystem.userinterface;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.Person;
import bankingsystem.domain.TarjetaCredito;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.services.CuentaServices;
import bankingsystem.view.*;
import bankingsystem.utils.PersonFormValidation;

import java.util.List;

public class MenuApp {
    private final PersonView personView;
    private final LoginView loginView;
    private final CuentaView cuentaView;

    private final CuentaServices cuentaService;


    public MenuApp(PersonView personView, LoginView loginView, CuentaView cuentaView, CuentaServices cuentaService) {
        this.personView = personView;
        this.loginView = loginView;
        this.cuentaView = cuentaView;
        this.cuentaService = cuentaService;
    }

    public void showMainMenu() {
        int option = -1;
        while (option != 0) {
            System.out.println("\n========================================");
            System.out.println("     SISTEMA BANCARIO MI PLATA");
            System.out.println("========================================");
            System.out.println("1. Iniciar Sesión");
            System.out.println("2. Registrarme");
            System.out.println("0. Salir");

            option = PersonFormValidation.validateInt("Seleccione una opción: ");

            switch (option) {
                case 1 -> handleLogin();
                case 2 -> personView.createPerson();
                case 0 -> System.out.println("Gracias por usar Mi Plata. ¡Hasta pronto!");
                default -> System.out.println("❌ Opción no válida.");
            }
        }
    }

    private void handleLogin() {
        Person user = loginView.executeLogin();
        if (user != null) {

            showUserSubMenu(user);
        } else {
            System.out.println("⚠️ Inicio de sesión fallido. Verifica tus credenciales o regístrate.");
            System.out.println("1. Registrarme");
            System.out.println("2. Volver al Menú Principal");
            int option = PersonFormValidation.validateInt("Seleccione: ");
            if (option == 1) personView.createPerson();
        }
    }

    private void showUserSubMenu(Person user) {
        int userOption = -1;
        while (userOption != 0) {
            System.out.println("\n========================================");
            System.out.println("✅ Sesión activa: " + user.getName());
            System.out.println("========================================");
            System.out.println("1. Ver Saldo / Estado");
            System.out.println("2. Consignar Dinero");
            System.out.println("3. Retirar Dinero");
            System.out.println("4. Ver Movimientos");
            System.out.println("5. Ver Todas mis Cuentas (Resumen)");
            System.out.println("6. Aperturar Tarjeta de Crédito");
            System.out.println("7. Submenú Tarjeta de Crédito");
            System.out.println("8. Aperturar Cuenta Faltante (Ahorros/Corriente)");
            System.out.println("9. Transferencias");
            System.out.println("0. Cerrar Sesión");

            userOption = PersonFormValidation.validateInt("Seleccione operación: ");

            if (userOption >= 1 && userOption <= 4) {
                showCuentasSubMenu(user, userOption);
            } else if (userOption == 5) {
                mostrarResumenCuentas(user.getUsername());
            } else if (userOption == 6) {
                aperturarTarjetaCredito(user.getUsername());
            } else if (userOption == 7) {
                showTarjetaCreditoSubMenu(user.getUsername());
            } else if (userOption == 8) {
                aperturarCuentaFaltante(user.getUsername());
            } else if (userOption == 9) {
                cuentaView.transferirDinero(user.getUsername());
            } else if (userOption == 0) {
                System.out.println("👋 Cerrando sesión...");
            } else {
                System.out.println("❌ Opción no válida.");
            }
        }
    }

    private void aperturarCuentaFaltante(String username) {
        Cuenta ahorro = this.cuentaService.obtenerCuentaPorTipo(username, TypoCuenta.AHORROS);
        Cuenta corriente = this.cuentaService.obtenerCuentaPorTipo(username, TypoCuenta.CORRIENTE);

        if (ahorro != null && corriente != null) {
            System.out.println("ℹ️ Ya tienes Cuenta de Ahorros y Cuenta Corriente.");
            return;
        }

        TypoCuenta tipoApertura;
        if (ahorro == null && corriente != null) {
            tipoApertura = TypoCuenta.AHORROS;
        } else if (corriente == null && ahorro != null) {
            tipoApertura = TypoCuenta.CORRIENTE;
        } else {
            System.out.println("No tienes cuentas base. Elige cuál deseas aperturar:");
            System.out.println("1. Cuenta de Ahorros");
            System.out.println("2. Cuenta Corriente");
            int opcion = PersonFormValidation.validateInt("Seleccione: ");
            tipoApertura = (opcion == 1) ? TypoCuenta.AHORROS : (opcion == 2) ? TypoCuenta.CORRIENTE : null;
            if (tipoApertura == null) {
                System.out.println("❌ Opción no válida.");
                return;
            }
        }

        double saldoInicial = PersonFormValidation.validateDouble("Ingrese saldo inicial para la nueva cuenta: ");

        try {
            this.cuentaService.crearCuenta(username, saldoInicial, tipoApertura);
            Cuenta nuevaCuenta = this.cuentaService.obtenerCuentaPorTipo(username, tipoApertura);
            String numero = (nuevaCuenta != null) ? nuevaCuenta.getNumeroCuenta() : "N/A";
            System.out.println("✅ " + tipoApertura + " aperturada correctamente. Número: " + numero + ".");
        } catch (Exception e) {
            System.out.println("❌ No fue posible aperturar la cuenta: " + e.getMessage());
        }
    }

    private void aperturarTarjetaCredito(String username) {
        Cuenta tarjetaExistente = this.cuentaService.obtenerCuentaPorTipo(username, TypoCuenta.TARJETA_CREDITO);
        if (tarjetaExistente != null) {
            System.out.println("ℹ️ Ya tienes una tarjeta de crédito activa.");
            return;
        }

        Cuenta ahorro = this.cuentaService.obtenerCuentaPorTipo(username, TypoCuenta.AHORROS);
        Cuenta corriente = this.cuentaService.obtenerCuentaPorTipo(username, TypoCuenta.CORRIENTE);
        if (ahorro == null && corriente == null) {
            System.out.println("⚠️ Debes tener una cuenta de ahorros o corriente para aperturar tarjeta.");
            return;
        }

        this.cuentaService.crearCuenta(username, 0, TypoCuenta.TARJETA_CREDITO);
        Cuenta tarjetaNueva = this.cuentaService.obtenerCuentaPorTipo(username, TypoCuenta.TARJETA_CREDITO);
        String numeroTarjeta = (tarjetaNueva != null) ? tarjetaNueva.getNumeroCuenta() : "N/A";
        System.out.println("✅ Tarjeta de crédito aperturada. Número: " + numeroTarjeta + " | Cupo asignado: $4,000,000.");
    }

    private void showTarjetaCreditoSubMenu(String username) {
        Cuenta tarjeta = this.cuentaService.obtenerCuentaPorTipo(username, TypoCuenta.TARJETA_CREDITO);
        if (tarjeta == null) {
            System.out.println("⚠️ Aún no tienes tarjeta de crédito.");
            int abrir = PersonFormValidation.validateInt("Digite 1 para aperturarla o 0 para volver: ");
            if (abrir == 1) {
                aperturarTarjetaCredito(username);
            }
            tarjeta = this.cuentaService.obtenerCuentaPorTipo(username, TypoCuenta.TARJETA_CREDITO);
            if (tarjeta == null) {
                return;
            }
        }

        int option = -1;
        while (option != 0) {
            System.out.println("\n--- SUBMENÚ TARJETA DE CRÉDITO ---");
            System.out.println("1. Ver Estado (Deuda y Cupo)");
            System.out.println("2. Realizar Compra Financiada");
            System.out.println("3. Pagar Deuda");
            System.out.println("4. Ver Movimientos");
            System.out.println("0. Volver");

            option = PersonFormValidation.validateInt("Seleccione: ");
            switch (option) {
                case 1 -> cuentaView.consultarEstadoTarjeta(username);
                case 2 -> cuentaView.realizarConsignacion(username, TypoCuenta.TARJETA_CREDITO);
                case 3 -> cuentaView.realizarRetiro(username, TypoCuenta.TARJETA_CREDITO);
                case 4 -> cuentaView.verMovimientos(username, TypoCuenta.TARJETA_CREDITO);
                case 0 -> System.out.println("↩️ Volviendo al menú de usuario...");
                default -> System.out.println("❌ Opción no válida.");
            }
        }
    }

    private void mostrarResumenCuentas(String username) {
        System.out.println("\n=== RESUMEN DE PRODUCTOS ===");


        List<Cuenta> misCuentas = this.cuentaService.listarTodasLasCuentas().stream()
                .filter(c -> c.getPropietario().equalsIgnoreCase(username))
                .toList();

        if (misCuentas.isEmpty()) {
            System.out.println("ℹ️ No tienes productos activos en este momento.");
        } else {
            for (Cuenta c : misCuentas) {
                if (c instanceof TarjetaCredito tc) {
                    System.out.printf("- [%s No: %s | Cupo Total: $%,.2f | Cupo Disponible: $%,.2f | Deuda Total: $%,.2f]%n",
                            c.getTipo(), c.getNumeroCuenta(), tc.getCupoTotal(), tc.getCupoDisponible(), tc.getSaldo());
                } else {
                    System.out.printf("- [%s No: %s | Saldo Total: $%,.2f]%n",
                            c.getTipo(), c.getNumeroCuenta(), c.getSaldo());
                }
            }
        }
    }

    private void showCuentasSubMenu(Person user, int operacion) {
        System.out.println("\n--- SELECCIONE EL PRODUCTO ---");
        System.out.println("1. Cuenta de Ahorros");
        System.out.println("2. Cuenta Corriente");

        int cuentaOption = PersonFormValidation.validateInt("Seleccione: ");

        TypoCuenta tipo = switch (cuentaOption) {
            case 1 -> TypoCuenta.AHORROS;
            case 2 -> TypoCuenta.CORRIENTE;
            default -> null;
        };

        if (tipo != null) {
            Cuenta cuentaExistente = this.cuentaService.obtenerCuentaPorTipo(user.getUsername(), tipo);

            if (cuentaExistente != null) {
                ejecutarAccionBancaria(user.getUsername(), operacion, tipo);
            } else {
                System.out.println("⚠️ No posees una cuenta de tipo " + tipo + ".");
                System.out.println("Usa la opción 8 del menú para aperturar la cuenta faltante.");
            }
        } else {
            System.out.println("❌ Opción de cuenta no válida.");
        }
    }

    private void ejecutarAccionBancaria(String username, int operacion, TypoCuenta tipoCuenta) {
        switch (operacion) {
            case 1 -> cuentaView.consultarEstado(username, tipoCuenta);
            case 2 -> cuentaView.realizarConsignacion(username, tipoCuenta);
            case 3 -> cuentaView.realizarRetiro(username, tipoCuenta);
            case 4 -> cuentaView.verMovimientos(username, tipoCuenta);
            default -> System.out.println("❌ Operación no válida.");
        }
    }
}