package bankingsystem.repository;

import bankingsystem.domain.CuentaAhorros;
import java.util.HashMap;
import java.util.Map;

public class CuentaAhorrosRepository {
    // Almacenamos por número de cuenta para acceso rápido
    private final Map<String, CuentaAhorros> ahorrosMap = new HashMap<>();

    public void save(CuentaAhorros cuenta) {
        ahorrosMap.put(cuenta.getNumeroCuenta(), cuenta);
    }

    public CuentaAhorros findByNumero(String numero) {
        return ahorrosMap.get(numero);
    }

    public boolean exists(String numero) {
        return ahorrosMap.containsKey(numero);
    }
}