package bankingsystem.Persistence.mapper;

import bankingsystem.domain.TarjetaCredito;
import bankingsystem.domain.enums.EstadoCuenta;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TarjetaCreditoRowMapper {

    public TarjetaCredito mapRow(ResultSet rs) throws SQLException {
        String numeroTarjeta = rs.getString("numero_tarjeta");
        String propietario = rs.getString("propietario");
        double limiteCredito = rs.getDouble("limite_credito");
        double saldoActual = rs.getDouble("saldo_actual");
        int activa = rs.getInt("activa");

        TarjetaCredito tarjeta = new TarjetaCredito(numeroTarjeta, propietario, limiteCredito);

        // saldo = deuda actual (lo que el cliente debe)
        tarjeta.setSaldo(saldoActual);

        // ✅ CORREGIDO: Mapeo seguro utilizando el Enum EstadoCuenta
        tarjeta.setEstado(activa == 1 ? EstadoCuenta.ACTIVA : EstadoCuenta.INACTIVA);

        return tarjeta;
    }
}