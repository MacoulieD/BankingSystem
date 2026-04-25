package bankingsystem.repository;

import bankingsystem.domain.CuentaCorriente;
import java.util.HashMap;
import java.util.Map;

public class CuentaCorrienteRepository {
    // Almacenamiento en memoria usando el número de cuenta como llave
    private final Map<String, CuentaCorriente> corrienteMap = new HashMap<>();

    /**
     * Guarda o actualiza una cuenta corriente en el repositorio.
     */
    public void save(CuentaCorriente cuenta) {
        corrienteMap.put(cuenta.getNumeroCuenta(), cuenta);
    }

    /**
     * Busca una cuenta corriente por su número único.
     * @return CuentaCorriente si existe, null en caso contrario.
     */
    public CuentaCorriente findCorrienteByNumero(String numero) {
        return corrienteMap.get(numero);
    }

    /**
     * Verifica si una cuenta ya existe para evitar duplicados en el registro.
     */
    public boolean exists(String numero) {
        return corrienteMap.containsKey(numero);
    }
}