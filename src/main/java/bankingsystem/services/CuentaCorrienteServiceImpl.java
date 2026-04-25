package bankingsystem.services;

import bankingsystem.domain.CuentaCorriente;
import bankingsystem.repository.CuentaCorrienteRepository; // Usamos el mismo repo general

public class CuentaCorrienteServiceImpl implements CuentaCorrienteService {
    private final CuentaCorrienteRepository repo;

    public CuentaCorrienteServiceImpl(CuentaCorrienteRepository repo) {
        this.repo = repo;
    }

    @Override
    public void retirarConSobregiro(String numero, double monto) {
        // Buscamos la cuenta (Asumiendo que el repo maneja CuentaCorriente también)
        CuentaCorriente cuenta = repo.findCorrienteByNumero(numero);

        if (cuenta == null) throw new RuntimeException("Cuenta Corriente no encontrada.");

        double limite = cuenta.getLimiteDisponible();
        if (monto > limite) {
            throw new RuntimeException("Cupo insuficiente. Su límite con sobregiro es: $" + limite);
        }

        cuenta.setSaldo(cuenta.getSaldo() - monto);
        cuenta.getMovimientos().add("Retiro (Sobregiro aplicado): -$" + monto + " | Nuevo saldo: $" + cuenta.getSaldo());
    }

    @Override
    public void consignar(String numero, double monto) {
        CuentaCorriente cuenta = repo.findCorrienteByNumero(numero);
        if (cuenta != null && monto > 0) {
            cuenta.setSaldo(cuenta.getSaldo() + monto);
            cuenta.getMovimientos().add("Consignación: +$" + monto);
        }
    }

    @Override
    public CuentaCorriente buscarCuenta(String numero) {
        return repo.findCorrienteByNumero(numero);
    }
}
