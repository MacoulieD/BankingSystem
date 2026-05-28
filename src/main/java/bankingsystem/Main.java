package bankingsystem;

import bankingsystem.Config.Config;
import bankingsystem.Persistence.database.DataBaseConnectionMySql;
import bankingsystem.userinterface.MenuApp;

public class Main {
    public static void main(String[] ignoredArgs) {

        System.out.println("========================================");
        System.out.println("   Iniciando Sistema Bancario Mi Plata  ");
        System.out.println("========================================");

        // 1. Llamamos a la instancia de la conexión
        DataBaseConnectionMySql db = DataBaseConnectionMySql.getInstance();

        // 2. Evaluamos si está conectado de verdad antes de lanzar el menú
        if (db.getConnection() != null) {
            System.out.println("✅ [BD] ¡Conexión exitosa a MySQL en XAMPP!");
        } else {
            System.out.println("❌ [BD] ERROR: No se pudo conectar a la base de datos.");
            System.out.println("⚠️  Verifica que Apache y MySQL estén activos en XAMPP.");
            System.out.println("========================================");
            // Opcional: puedes usar System.exit(1); si quieres detener el programa aquí si no hay BD
        }
        System.out.println("========================================\n");

        // Inicializamos la aplicación mediante la configuración
        MenuApp app = Config.createMenuApp();

        // Arrancamos el flujo principal
        app.showMainMenu();
    }
}