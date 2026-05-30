package bankingsystem.Persistence.repository;

import bankingsystem.Persistence.mapper.TarjetaCreditoRowMapper;
import bankingsystem.domain.TarjetaCredito;
import bankingsystem.services.outputport.TarjetaCreditoPersistencePort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TarjetaCreditoRepositoryAdapterMySql implements TarjetaCreditoPersistencePort {

    private final Connection dbConnection;
    private final TarjetaCreditoRowMapper rowMapper;

    public TarjetaCreditoRepositoryAdapterMySql(Connection dbConnection, TarjetaCreditoRowMapper rowMapper) {
        this.dbConnection = dbConnection;
        this.rowMapper = rowMapper;
    }

    @Override
    public TarjetaCredito saveTarjeta(TarjetaCredito tarjeta) {
        String sqlCuentas = "INSERT INTO cuentas (numero_cuenta, propietario, tipo_cuenta, saldo, estado) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE saldo = VALUES(saldo), estado = VALUES(estado)";
        String sqlTarjeta = "INSERT INTO tarjetas_credito (numero_tarjeta, propietario, cvv, limite_credito, deuda_actual, fecha_vencimiento, activa) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE deuda_actual = VALUES(deuda_actual), activa = VALUES(activa)";

        double cupoTotal = tarjeta.getCupoTotal(0);
        double deuda = tarjeta.getSaldo();
        String estado = tarjeta.getEstado() != null ? tarjeta.getEstado().name() : "ACTIVA";
        int activa = estado.equalsIgnoreCase("ACTIVA") ? 1 : 0;

        try {
            dbConnection.setAutoCommit(false);

            try (PreparedStatement ps = dbConnection.prepareStatement(sqlCuentas)) {
                ps.setString(1, tarjeta.getNumeroCuenta());
                ps.setString(2, tarjeta.getPropietario());
                ps.setString(3, "TARJETA_CREDITO");
                ps.setDouble(4, deuda);
                ps.setString(5, estado);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = dbConnection.prepareStatement(sqlTarjeta)) {
                ps.setString(1, tarjeta.getNumeroCuenta());
                ps.setString(2, tarjeta.getPropietario());
                ps.setString(3, "123");
                ps.setDouble(4, cupoTotal);
                ps.setDouble(5, deuda);
                ps.setString(6, "12-31");
                ps.setInt(7, activa);
                ps.executeUpdate();
            }

            dbConnection.commit();
            return tarjeta;

        } catch (SQLException e) {
            try { dbConnection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Error al persistir la tarjeta de crédito: " + e.getMessage(), e);
        } finally {
            try { dbConnection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public TarjetaCredito findByNumero(String numeroTarjeta) {
        String sql = "SELECT * FROM tarjetas_credito WHERE numero_tarjeta = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, numeroTarjeta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar tarjeta de crédito por número: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<TarjetaCredito> findByPropietario(String username) {
        List<TarjetaCredito> listaTarjetas = new ArrayList<>();
        String sql = "SELECT * FROM tarjetas_credito WHERE LOWER(TRIM(propietario)) = LOWER(TRIM(?))";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaTarjetas.add(rowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las tarjetas de crédito del propietario: " + e.getMessage(), e);
        }
        return listaTarjetas;
    }
}