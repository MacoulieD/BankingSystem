package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.CuentaAhorros;
import bankingsystem.domain.CuentaCorriente;
import bankingsystem.repository.CuentaCorrienteRepository;
import bankingsystem.repository.CuentaRepository;


public class CuentaCorrienteServiceImpl implements CuentaCorrienteServices {

    private final CuentaCorrienteRepository repo;

    public CuentaCorrienteServiceImpl(CuentaCorrienteRepository repo) {
        this.repo = repo;
    }


    @Override
    public void crearCuenta(String username, double saldoInicial) {

        String numCuenta = "CC-" + Math.abs(username.hashCode());


        double cupoSobregiro = 500000.0;
        CuentaCorriente cuenta = new CuentaCorriente(numCuenta, saldoInicial, username, cupoSobregiro);


        repo.saveCuentaC(cuenta);

        System.out.println("[Backend] Cuenta Corriente guardada exitosamente para: " + username);
    }

    @Override
    public void retirarConSobregiro(String username, double monto) {

        CuentaCorriente cuenta = (CuentaCorriente) obtenerCuenta(username);

        if (cuenta == null) {
            throw new RuntimeException("No se encontró una cuenta corriente para el usuario: " + username);
        }

        // El límite disponible es Saldo + Cupo de Sobregiro
        //Correcion sobre el limite de sobre giro no lo esta tomando en cuenta arreglar
        double limiteTotal = cuenta.getSaldo() + cuenta.getCupoSobregiro();

        if (monto > limiteTotal) {
            throw new RuntimeException("🚫 Operación rechazada: Fondos insuficientes (Límite con sobregiro: $" + limiteTotal + ")");
        }

        // Aplicamos el retiro (el saldo puede quedar en negativo)
        //No esta aplicando el retiro correctamente, ya que no esta tomando en cuenta el limite de sobre giro arreglar
        cuenta.setSaldo(cuenta.getSaldo() - monto);

        String log = String.format("Retiro: -$%.2f | Nuevo saldo: $%.2f", monto, cuenta.getSaldo());
        cuenta.getMovimientos().add(log);
    }

    @Override
    public void consignar(String username, double monto) {
        CuentaCorriente cuenta = (CuentaCorriente) obtenerCuenta(username);

        if (cuenta == null) {
            String numCuenta = "CC-" + Math.abs(username.hashCode());
            cuenta = new CuentaCorriente(numCuenta, 0, username, 500000.0); // 500k de sobregiro inicial
            repo.saveCuentaC(cuenta);
        }

        if (monto > 0) {
            cuenta.setSaldo(cuenta.getSaldo() + monto);
            cuenta.getMovimientos().add(String.format("Consignación: +$%.2f", monto));
        }
    }
    @Override
    public Cuenta consultarCuenta(String username) {
        return null;
    }
    @Override
    public Cuenta obtenerCuenta(String username) {
        return null;
    }

    @Override
    public CuentaCorriente buscarCuenta(String username) {
        return repo.findCuentaCorriente();
    }
}