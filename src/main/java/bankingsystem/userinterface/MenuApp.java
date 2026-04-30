package bankingsystem.userinterface;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.Person;
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
                default -> System.out.println("❎ Opción no válida.");
            }
        }
    }

    private void handleLogin() {
        Person user = loginView.executeLogin();
        if (user != null) {

            showUserSubMenu(user);
        } else {
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
            System.out.println("0. Cerrar Sesión");

            userOption = PersonFormValidation.validateInt("Seleccione operación: ");

            if (userOption >= 1 && userOption <= 4) {
                showCuentasSubMenu(user, userOption);
            } else if (userOption == 5) {
                mostrarResumenCuentas(user.getUsername());
            } else if (userOption == 0) {
                System.out.println("👋 Cerrando sesión...");
            } else {
                System.out.println("❎ Opción no válida.");
            }
        }
    }

    private void mostrarResumenCuentas(String username) {
        System.out.println("\n=== RESUMEN DE PRODUCTOS ===");


        List<Cuenta> misCuentas = this.cuentaService.listarTodasLasCuentas().stream()
                .filter(c -> c.getPropietario().equalsIgnoreCase(username))
                .toList();

        if (misCuentas.isEmpty()) {
            System.out.println("No tienes productos activos en este momento.");
        } else {
            for (Cuenta c : misCuentas) {
                System.out.printf("- [%s No: %s | Saldo/Deuda: $%,.2f%n",
                        c.getTipo(), c.getNumeroCuenta(), c.getSaldo());
            }
        }
    }

    private void showCuentasSubMenu(Person user, int operacion) {
        System.out.println("\n--- SELECCIONE EL PRODUCTO ---");
        System.out.println("1. Cuenta de Ahorros");
        System.out.println("2. Cuenta Corriente");
        System.out.println("3. Tarjeta de Crédito");

        int cuentaOption = PersonFormValidation.validateInt("Seleccione: ");

        TypoCuenta tipo = switch (cuentaOption) {
            case 1 -> TypoCuenta.AHORROS;
            case 2 -> TypoCuenta.CORRIENTE;
            case 3 -> TypoCuenta.TARJETA_CREDITO;
            default -> null;
        };

        if (tipo != null) {
            Cuenta cuentaExistente = this.cuentaService.obtenerCuentaPorTipo(user.getUsername(), tipo);

            if (cuentaExistente != null) {
                ejecutarAccionBancaria(user.getUsername(), operacion, tipo);
            } else {
                System.out.println("⚠️ Error: No posees una cuenta de tipo " + tipo + ". Debes crearla primero.");
            }
        } else {
            System.out.println("❌ Opción de cuenta no válida.");
        }
    }

    private void ejecutarAccionBancaria(String username, int operacion, TypoCuenta tipoCuenta) {
        String tipoStr = tipoCuenta.toString();

        switch (operacion) {
            case 1 -> cuentaView.consultarEstado(username, tipoStr);
            case 2 -> cuentaView.realizarConsignacion(username, tipoStr);
            case 3 -> cuentaView.realizarRetiro(username, tipoStr);
            case 4 -> cuentaView.verMovimientos(username, tipoStr);
            default -> System.out.println("Operación no válida.");
        }
    }
}