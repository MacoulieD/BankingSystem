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
    public void saveCuentaAhorros(CuentaAhorros cuenta) {
        // 1. INSERT en cuentas primero (FK: cuenta_ahorros.numero_cuenta → cuentas.numero_cuenta)
        String sqlCuentas = "INSERT INTO cuentas (numero_cuenta, propietario, tipo_cuenta, saldo, estado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(sqlCuentas)) {
            ps.setString(1, cuenta.getNumeroCuenta());
            ps.setString(2, cuenta.getPropietario());
            ps.setString(3, "AHORROS");
            ps.setDouble(4, cuenta.getSaldo());
            ps.setString(5, cuenta.getEstado().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar en tabla cuentas: " + e.getMessage(), e);
        }

        // 2. INSERT en cuenta_ahorros
        String sqlAhorros = "INSERT INTO cuenta_ahorros (numero_cuenta, propietario, saldo, tasa_interes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(sqlAhorros)) {
            ps.setString(1, cuenta.getNumeroCuenta());
            ps.setString(2, cuenta.getPropietario());
            ps.setDouble(3, cuenta.getSaldo());
            ps.setDouble(4, cuenta.getTasaInteres());
            ps.executeUpdate();
            System.out.println("✅ Cuenta de Ahorros guardada en base de datos. Número: " + cuenta.getNumeroCuenta());
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar en tabla cuenta_ahorros: " + e.getMessage(), e);
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
        String sql = "SELECT numero_cuenta, propietario, saldo FROM cuenta_ahorros WHERE propietario = ?";
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
