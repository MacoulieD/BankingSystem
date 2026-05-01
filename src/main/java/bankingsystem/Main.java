package bankingsystem;

import bankingsystem.Config.Config;
import bankingsystem.userinterface.MenuApp;

public class Main {
    public static void main(String[] ignoredArgs) {

        // Inicializamos la aplicación mediante la configuración
        MenuApp app = Config.createMenuApp();

        // Arrancamos el flujo principal
        app.showMainMenu();
    }
}