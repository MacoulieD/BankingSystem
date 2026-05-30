package bankingsystem.Persistence.mapper;

import bankingsystem.domain.Movimiento;
import bankingsystem.domain.enums.TipoMovimiento;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MovimientoRowMapper implements RowMapper<Movimiento> {

    @Override
    public Movimiento mapRow(ResultSet rs) throws SQLException {
        int id                  = rs.getInt("id");
        String tipoStr          = rs.getString("tipo_movimiento");
        double monto            = rs.getDouble("monto");
        double saldoPosterior   = rs.getDouble("saldo_posterior");
        String descripcion      = rs.getString("descripcion");

        TipoMovimiento tipo;
        try {
            tipo = TipoMovimiento.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            tipo = TipoMovimiento.CONSIGNACION; // fallback defensivo
        }

        return new Movimiento(id, tipo, monto, saldoPosterior, descripcion);
    }
}