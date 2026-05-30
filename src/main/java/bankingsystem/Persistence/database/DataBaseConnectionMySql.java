package bankingsystem.Persistence.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// Clase para manejar la conexión a la base de datos MySQL utilizando el patrón Singleton
public class DataBaseConnectionMySql {
// Instancia única de la clase
    private static DataBaseConnectionMySql instance;
    private final Connection connection;

    private static final String URL      = "jdbc:mysql://localhost:3307/banking_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private DataBaseConnectionMySql() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar la base de datos", e);
        }
    }

    public static synchronized DataBaseConnectionMySql getInstance() {
        if (instance == null) {
            instance = new DataBaseConnectionMySql();
        }
        return instance;
    }

    public Connection getConnection() {
        System.out.println("Conectado a la base de datos");
        return connection;
    }

}