package bankingsystem.Persistence.repository;

import bankingsystem.Persistence.mapper.MovimientoRowMapper;
import bankingsystem.domain.Movimiento;
import bankingsystem.domain.enums.TipoMovimiento;
import bankingsystem.services.outputport.MovimientoPersistencePort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovimientoRepositoryAdapterMySql implements MovimientoPersistencePort {

    private final Connection dbConnection;
    private final MovimientoRowMapper rowMapper;

    public MovimientoRepositoryAdapterMySql(Connection dbConnection, MovimientoRowMapper rowMapper) {
        this.dbConnection = dbConnection;
        this.rowMapper    = rowMapper;
    }

    // ── Criterio 1: guarda el movimiento en la BD ──────────────────────────────
    @Override
    public void saveMovimiento(String numeroCuenta, Movimiento movimiento) {
        String sql = "INSERT INTO movimientos " +
                "(numero_cuenta, tipo_movimiento, monto, saldo_anterior, saldo_posterior, descripcion) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {

            // ✅ Cálculo correcto de saldo_anterior según el tipo de movimiento:
            // - CONSIGNACION / TRANSFERENCIA_IN / PAGO_TC  → el saldo subió  → anterior = posterior - monto
            // - RETIRO / TRANSFERENCIA_OUT / COMPRA_TC     → el saldo bajó   → anterior = posterior + monto
            double saldoAnterior;
            TipoMovimiento tipo = movimiento.getTipo();

            if (tipo == TipoMovimiento.CONSIGNACION
                    || tipo == TipoMovimiento.TRANSFERENCIA_IN
                    || tipo == TipoMovimiento.PAGO_TC) {
                saldoAnterior = movimiento.getSaldoPosterior() - movimiento.getValor();
            } else {
                // RETIRO, TRANSFERENCIA_OUT, COMPRA_TC
                saldoAnterior = movimiento.getSaldoPosterior() + movimiento.getValor();
            }

            ps.setString(1, numeroCuenta);
            ps.setString(2, tipo.name());
            ps.setDouble(3, movimiento.getValor());
            ps.setDouble(4, saldoAnterior);
            ps.setDouble(5, movimiento.getSaldoPosterior());
            ps.setString(6, movimiento.getDescripcion());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el movimiento en BD: " + e.getMessage(), e);
        }
    }

    // ── Criterio 1: trae movimientos desde la BD por número de cuenta ──────────
    @Override
    public List<Movimiento> findByNumeroCuenta(String numeroCuenta) {
        String sql = "SELECT id, tipo_movimiento, monto, saldo_anterior, saldo_posterior, descripcion " +
                "FROM movimientos " +
                "WHERE numero_cuenta = ? " +
                "ORDER BY fecha ASC";

        List<Movimiento> resultado = new ArrayList<>();

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, numeroCuenta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(rowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar movimientos desde BD: " + e.getMessage(), e);
        }

        return resultado;
    }
}