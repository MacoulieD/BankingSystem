package bankingsystem.userinterface;

import bankingsystem.domain.Person;
import bankingsystem.view.CuentaAhorrosView;
import bankingsystem.view.LoginView;
import bankingsystem.view.PersonView;
import bankingsystem.utils.PersonFormValidation;

public class MenuApp {
    private final PersonView personView;
    private final LoginView loginView;
    private final CuentaAhorrosView cuentaView; // 1. Atributo agregado

    // 2. Constructor corregido con la asignación de cuentaView
    public MenuApp(PersonView personView, LoginView loginView, CuentaAhorrosView cuentaView) {
        this.personView = personView;
        this.loginView = loginView;
        this.cuentaView = cuentaView;
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

        if (user == null) {
            System.out.println("\n[SISTEMA]: No se pudo completar el acceso.");
            System.out.println("1. Registrarme");
            System.out.println("2. Volver al Menú Principal");

            int choice = PersonFormValidation.validateInt("Seleccione: ");

            if (choice == 1) {
                System.out.println("\n--- REDIRIGIENDO A REGISTRO ---");
                personView.createPerson();
            }
        } else {
            showUserSubMenu(user);
        }
    }

    private void showUserSubMenu(Person user) {
        int userOption = -1;
        // Supongamos que usamos el username o un ID para identificar la cuenta vinculada
        String accountId = user.getUsername();

        while (userOption != 0) {
            System.out.println("\n========================================");
            System.out.println("✅ Sesión activa: " + user.getName());
            System.out.println("========================================");
            System.out.println("1. Ver Saldo y Movimientos");
            System.out.println("2. Consignar Dinero");
            System.out.println("3. Retirar Dinero (Ahorros)");
            System.out.println("0. Cerrar Sesión");

            userOption = PersonFormValidation.validateInt("Seleccione operación: ");

            switch (userOption) {
                // 3. Conexión con CuentaAhorrosView
                case 1 -> cuentaView.consultarEstado(accountId);
                case 2 -> {
                    // Implementaremos consignarView.ejecutar() similar al retiro
                    System.out.println("🚧 Módulo de Consignación en desarrollo...");
                }
                case 3 -> cuentaView.realizarRetiro(accountId);
                case 0 -> System.out.println("Cerrando sesión de " + user.getUsername() + "...");
                default -> System.out.println("❎ Opción no válida.");
            }
        }
    }
}