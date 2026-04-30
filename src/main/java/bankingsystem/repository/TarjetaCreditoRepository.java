package bankingsystem.repository;

import bankingsystem.domain.TarjetaCredito;
import java.util.ArrayList;
import java.util.List;

public class TarjetaCreditoRepository {
    private final List<TarjetaCredito> tarjetas = new ArrayList<>();

    public void saveTarjeta(TarjetaCredito tarjeta) {
        this.tarjetas.add(tarjeta);
    }

    public List<TarjetaCredito> findAllTarjetas() {
        return new ArrayList<>(tarjetas);
    }
    public TarjetaCredito findByPropietario(String username) {
        return tarjetas.stream()
                .filter(t -> t.getPropietario().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }
}