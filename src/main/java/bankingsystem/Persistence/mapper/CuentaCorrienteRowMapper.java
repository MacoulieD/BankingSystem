package bankingsystem.Persistence.mapper;

import bankingsystem.domain.CuentaCorriente;
import bankingsystem.domain.enums.TypoCuenta;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CuentaCorrienteRowMapper {

    public CuentaCorriente mapRow(ResultSet rs) throws SQLException {
        String numeroCuenta = rs.getString("numero_cuenta");
        double saldo = rs.getDouble("saldo");
        String propietario = rs.getString("propietario");
        double sobregiro = rs.getDouble("sobregiro"); // Asegúrate de que en tu BD se llame 'sobregiro'

        CuentaCorriente cuentaCorriente = new CuentaCorriente(numeroCuenta, saldo, propietario, sobregiro);
        cuentaCorriente.setTipo(TypoCuenta.CORRIENTE);

        return cuentaCorriente;
    }
}