package bankingsystem.repository;

import bankingsystem.domain.TarjetaCredito;
import java.util.HashMap;
import java.util.Map;

public class TargetaCreditoRepository {

    // Almacenamos las tarjetas usando el número de tarjeta como clave (Key)
    private final Map<String, TarjetaCredito> tarjetaMap = new HashMap<>();

    /**
     * Guarda una nueva tarjeta o actualiza una existente.
     * @param tarjeta Objeto con los datos de la tarjeta de crédito.
     */
    public void save(TarjetaCredito tarjeta) {
        tarjetaMap.put(tarjeta.getNumeroTarjeta(), tarjeta);
    }

    /**
     * Busca una tarjeta de crédito por su número único.
     * @param numero El número de tarjeta a buscar.
     * @return El objeto TarjetaCredito si existe, o null si no se encuentra.
     */
    public TarjetaCredito findByNumero(String numero) {
        return tarjetaMap.get(numero);
    }

    /**
     * Verifica si una tarjeta ya está registrada en el sistema.
     * Útil para procesos de validación durante la creación de productos.
     */
    public boolean exists(String numero) {
        return tarjetaMap.containsKey(numero);
    }

    /**
     * Retorna el total de tarjetas registradas (opcional, para auditoría).
     */
    public int count() {
        return tarjetaMap.size();
    }
}