package bankingsystem.repository;

import bankingsystem.domain.Movimiento;
import java.util.ArrayList;
import java.util.List;

public class MovimientoRepository {

    private final List<Movimiento> movimientos = new ArrayList<>();

    public void save(Movimiento movimiento) {
        movimientos.add(movimiento);
    }

    public List<Movimiento> findAll() {
        return movimientos;
    }
}