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
        String sql = "INSERT INTO tarjetas_credito (numero_tarjeta, propietario, cvv, limite_credito, saldo_actual, fecha_vencimiento, activa) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE saldo_actual = ?, activa = ?";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            // ✅ CORREGIDO: Cambiado a getNumeroCuenta() según la estructura de tu entidad
            ps.setString(1, tarjeta.getNumeroCuenta());
            ps.setString(2, tarjeta.getPropietario());

            ps.setString(3, "123");

            double cupoTotal = tarjeta.getCupoTotal(0);
            ps.setDouble(4, cupoTotal);

            // saldo_actual en BD = deuda = getSaldo()
            ps.setDouble(5, tarjeta.getSaldo());

            ps.setString(6, "12-31");

            // ✅ CORREGIDO: Manejo correcto extrayendo el name() del Enum EstadoCuenta
            int estadoActiva = (tarjeta.getEstado() != null && tarjeta.getEstado().name().equalsIgnoreCase("ACTIVA")) ? 1 : 0;
            ps.setInt(7, estadoActiva);

            // Parámetros para el UPDATE
            ps.setDouble(8, tarjeta.getSaldo());
            ps.setInt(9, estadoActiva);

            ps.executeUpdate();

            // ✅ CORREGIDO: Cambiado también en el print del sistema
            System.out.println("✅ Tarjeta de Crédito guardada/actualizada en MySQL: " + tarjeta.getNumeroCuenta());
            return tarjeta;

        } catch (SQLException e) {
            throw new RuntimeException("Error al persistir la tarjeta de crédito: " + e.getMessage(), e);
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