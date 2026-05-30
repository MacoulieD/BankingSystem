package bankingsystem.Persistence.repository;

import bankingsystem.Persistence.mapper.CuentaAhorrosRowMapper;
import bankingsystem.domain.CuentaAhorros;
import bankingsystem.services.outputport.CuentaAhorrosPersistencePort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CuentaAhorrosRepositoryAdapterMySql implements CuentaAhorrosPersistencePort {

    private final Connection dbConnection;
    private final CuentaAhorrosRowMapper rowMapper;

    public CuentaAhorrosRepositoryAdapterMySql(Connection dbConnection, CuentaAhorrosRowMapper rowMapper) {
        this.dbConnection = dbConnection;
        this.rowMapper = rowMapper;
    }

    @Override
    public CuentaAhorros saveCuentaAhorros(CuentaAhorros cuenta) {
        // ON DUPLICATE KEY UPDATE → si la cuenta ya existe solo actualiza el saldo
        String sqlCuentas = "INSERT INTO cuentas (numero_cuenta, propietario, tipo_cuenta, saldo, estado) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE saldo = VALUES(saldo), estado = VALUES(estado)";
        String sqlAhorros = "INSERT INTO cuenta_ahorros (numero_cuenta, propietario, saldo, tasa_interes) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE saldo = VALUES(saldo)";

        try {
            dbConnection.setAutoCommit(false);

            try (PreparedStatement ps = dbConnection.prepareStatement(sqlCuentas)) {
                ps.setString(1, cuenta.getNumeroCuenta());
                ps.setString(2, cuenta.getPropietario());
                ps.setString(3, "AHORROS");
                ps.setDouble(4, cuenta.getSaldo());
                ps.setString(5, cuenta.getEstado() != null ? cuenta.getEstado().name() : "ACTIVA");
                ps.executeUpdate();
            }

            try (PreparedStatement ps = dbConnection.prepareStatement(sqlAhorros)) {
                ps.setString(1, cuenta.getNumeroCuenta());
                ps.setString(2, cuenta.getPropietario());
                ps.setDouble(3, cuenta.getSaldo());
                ps.setDouble(4, cuenta.getTasaInteres());
                ps.executeUpdate();
            }

            dbConnection.commit();
            return cuenta;

        } catch (SQLException e) {
            try {
                dbConnection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Error transaccional al guardar la cuenta de ahorros: " + e.getMessage(), e);
        } finally {
            try {
                dbConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int countCuentas() {
        String sql = "SELECT COUNT(*) FROM cuentas";
        try (PreparedStatement ps = dbConnection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar cuentas: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public CuentaAhorros findByPropietario(String username) {
        String sql = "SELECT c.numero_cuenta, c.propietario, c.saldo, c.estado, ca.tasa_interes " +
                "FROM cuentas c " +
                "INNER JOIN cuenta_ahorros ca ON c.numero_cuenta = ca.numero_cuenta " +
                "WHERE LOWER(TRIM(c.propietario)) = LOWER(TRIM(?))";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cuenta de ahorros: " + e.getMessage(), e);
        }
        return null;
    }
}