package bankingsystem.Persistence.repository;

import bankingsystem.Persistence.mapper.CuentaCorrienteRowMapper;
import bankingsystem.domain.CuentaCorriente;
import bankingsystem.services.outputport.CuentaCorrientePersistencePort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CuentaCorrienteRepositoryAdapterMySql implements CuentaCorrientePersistencePort {

    private final Connection dbConnection;
    private final CuentaCorrienteRowMapper rowMapper;

    public CuentaCorrienteRepositoryAdapterMySql(Connection dbConnection, CuentaCorrienteRowMapper rowMapper) {
        this.dbConnection = dbConnection;
        this.rowMapper = rowMapper;
    }

    @Override
    public CuentaCorriente saveCuentaC(CuentaCorriente cuenta) {
        // ON DUPLICATE KEY UPDATE → si la cuenta ya existe solo actualiza el saldo
        String sqlCuentas  = "INSERT INTO cuentas (numero_cuenta, propietario, tipo_cuenta, saldo, estado) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE saldo = VALUES(saldo), estado = VALUES(estado)";
        String sqlCorriente = "INSERT INTO cuenta_corriente (numero_cuenta, propietario, saldo, sobregiro_permitido) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE saldo = VALUES(saldo)";

        try {
            dbConnection.setAutoCommit(false);

            try (PreparedStatement ps = dbConnection.prepareStatement(sqlCuentas)) {
                ps.setString(1, cuenta.getNumeroCuenta());
                ps.setString(2, cuenta.getPropietario());
                ps.setString(3, "CORRIENTE");
                ps.setDouble(4, cuenta.getSaldo());
                ps.setString(5, cuenta.getEstado() != null ? cuenta.getEstado().name() : "ACTIVA");
                ps.executeUpdate();
            }

            try (PreparedStatement ps = dbConnection.prepareStatement(sqlCorriente)) {
                ps.setString(1, cuenta.getNumeroCuenta());
                ps.setString(2, cuenta.getPropietario());
                ps.setDouble(3, cuenta.getSaldo());
                ps.setDouble(4, cuenta.getCupoSobregiro());
                ps.executeUpdate();
            }

            dbConnection.commit();
            System.out.println("✅ Cuenta Corriente guardada en base de datos. Número: " + cuenta.getNumeroCuenta());
            return cuenta;

        } catch (SQLException e) {
            try {
                dbConnection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Error transaccional al guardar la cuenta corriente: " + e.getMessage(), e);
        } finally {
            try {
                dbConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public CuentaCorriente findbypropietario(String username) {
        // 🛠️ Mapeamos 'cc.sobregiro_permitido AS sobregiro' para que el RowMapper funcione sin tocarlo
        String sql = "SELECT c.numero_cuenta, c.propietario, c.saldo, c.estado, cc.sobregiro_permitido AS sobregiro " +
                "FROM cuentas c " +
                "INNER JOIN cuenta_corriente cc ON c.numero_cuenta = cc.numero_cuenta " +
                "WHERE LOWER(TRIM(c.propietario)) = LOWER(TRIM(?))";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cuenta corriente por propietario: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public int countCuentas() {
        String sql = "SELECT COUNT(*) FROM cuentas";
        try (PreparedStatement ps = dbConnection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar las cuentas desde cuenta corriente: " + e.getMessage(), e);
        }
        return 0;
    }
}