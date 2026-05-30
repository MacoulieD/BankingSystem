package bankingsystem.Persistence.mapper;

import bankingsystem.domain.CuentaAhorros;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CuentaAhorrosRowMapper implements RowMapper<CuentaAhorros> {

    @Override
    public CuentaAhorros mapRow(ResultSet rs) throws SQLException {
        CuentaAhorros cuenta = new CuentaAhorros(
                rs.getString("numero_cuenta"),
                rs.getDouble("saldo"),
                rs.getString("propietario")
        );
        return cuenta;
    }
}
