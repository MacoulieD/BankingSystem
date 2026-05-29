package bankingsystem.Persistence.repository;

import bankingsystem.domain.CuentaAhorros;
import bankingsystem.services.outputport.CuentaAhorrosPersistencePort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CuentaAhorrosRepository implements CuentaAhorrosPersistencePort {

    private final Connection connection;

    public CuentaAhorrosRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public CuentaAhorros saveCuentaAhorros(CuentaAhorros cuenta) {
        String sqlCuenta = "INSERT INTO cuentas (numero_cuenta, saldo, propietario, tipo) VALUES (?, ?, ?, ?)";
        String sqlAhorros = "INSERT INTO cuenta_ahorros (numero_cuenta, saldo_inicial) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psCuenta = connection.prepareStatement(sqlCuenta)) {
                psCuenta.setString(1, cuenta.getNumeroCuenta());
                psCuenta.setDouble(2, cuenta.getSaldo());
                psCuenta.setString(3, cuenta.getPropietario());
                psCuenta.setString(4, "AHORROS");
                psCuenta.executeUpdate();
            }

            try (PreparedStatement psAhorros = connection.prepareStatement(sqlAhorros)) {
                psAhorros.setString(1, cuenta.getNumeroCuenta());
                psAhorros.setDouble(2, cuenta.getSaldo());
                psAhorros.executeUpdate();
            }

            connection.commit();
            System.out.println("💾 ¡Cuenta de ahorros guardada físicamente en MySQL!");
            return cuenta;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("❌ Error al insertar la cuenta en la base de datos: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int countCuentas() {
        String sql = "SELECT COUNT(*) FROM cuentas";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar las cuentas: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public CuentaAhorros findByPropietario(String username) {
        String sql = "SELECT * FROM cuentas WHERE LOWER(TRIM(propietario)) = LOWER(TRIM(?)) AND tipo = 'AHORROS'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CuentaAhorros cuenta = new CuentaAhorros(
                            rs.getString("numero_cuenta"),
                            rs.getDouble("saldo"),
                            rs.getString("propietario")
                    );
                    cuenta.setTipo(bankingsystem.domain.enums.TypoCuenta.AHORROS);
                    return cuenta;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cuenta por propietario: " + e.getMessage());
        }
        return null;
    }

    public void saveCuentaA(CuentaAhorros cuentaahorros) {
        saveCuentaAhorros(cuentaahorros);
    }

    public CuentaAhorros findCuentaAhorrosByNumCuenta(String numCuenta) {
        String sql = "SELECT * FROM cuentas WHERE numero_cuenta = ? AND tipo = 'AHORROS'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, numCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CuentaAhorros cuenta = new CuentaAhorros(
                            rs.getString("numero_cuenta"),
                            rs.getDouble("saldo"),
                            rs.getString("propietario")
                    );
                    cuenta.setTipo(bankingsystem.domain.enums.TypoCuenta.AHORROS);
                    return cuenta;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cuenta por número: " + e.getMessage());
        }
        return null;
    }

    public CuentaAhorros fidByPropietario(String username) {
        return findByPropietario(username);
    }
}