package bankingsystem.services.outputport;

import bankingsystem.domain.TarjetaCredito;
import java.util.List;

public interface TarjetaCreditoPersistencePort {
    TarjetaCredito saveTarjeta(TarjetaCredito tarjeta);
    TarjetaCredito findByNumero(String numeroTarjeta);
    List<TarjetaCredito> findByPropietario(String username);
}