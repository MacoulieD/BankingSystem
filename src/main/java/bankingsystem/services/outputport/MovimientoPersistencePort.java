package bankingsystem.services.outputport;

import bankingsystem.domain.Movimiento;

import java.util.List;

public interface MovimientoPersistencePort {

    /**
     * Persiste un movimiento en la base de datos.
     */
    void saveMovimiento(String numeroCuenta, Movimiento movimiento);

    /**
     * Retorna todos los movimientos asociados a un número de cuenta.
     */
    List<Movimiento> findByNumeroCuenta(String numeroCuenta);
}