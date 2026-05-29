package bankingsystem.Persistence.mapper;

import bankingsystem.domain.CuentaAhorros;
import bankingsystem.domain.enums.EstadoCuenta;
import bankingsystem.domain.enums.TypoCuenta;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CuentaAhorrosRowMapper implements RowMapper<CuentaAhorros> {

    public CuentaAhorros mapRow(ResultSet rs) throws SQLException {
        // 1. Extraemos los datos de las columnas con los nombres exactos de la BD
        String numeroCuenta = rs.getString("numero_cuenta");
        double saldo = rs.getDouble("saldo");
        String propietario = rs.getString("propietario");
        double tasaInteres = rs.getDouble("tasa_interes");

        // 2. Instanciamos el objeto usando el constructor de tu dominio
        CuentaAhorros cuentaAhorros = new CuentaAhorros(numeroCuenta, saldo, propietario);

        // 3. Forzamos el tipo de cuenta para que el dominio sepa qué es
        cuentaAhorros.setTipo(TypoCuenta.AHORROS);

        // 4. Mapeo seguro del Estado (Evita caídas si el string en la BD está vacío)
        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            try {
                cuentaAhorros.setEstado(EstadoCuenta.valueOf(estadoStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                cuentaAhorros.setEstado(EstadoCuenta.ACTIVA); // Estado por defecto si hay error
            }
        } else {
            cuentaAhorros.setEstado(EstadoCuenta.ACTIVA);
        }

        // 5. Devolvemos el objeto completamente construido
        return cuentaAhorros;
    }
}
