package bankingsystem.services;

import bankingsystem.domain.*;

import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.repository.*;

import java.util.List;


public class CuentaServicesImpl implements CuentaServices {

    private final CuentaRepository repository;
    private final CuentaAhorrosRepository ahorrosRepo;
    private final CuentaCorrienteRepository corrienteRepo;
    private final TarjetaCreditoRepository tarjetaRepo;

    public CuentaServicesImpl(CuentaRepository repository,
                              CuentaAhorrosRepository ahorrosRepo,
                              CuentaCorrienteRepository corrienteRepo,
                              TarjetaCreditoRepository tarjetaRepo) { // <-- AGREGA ESTO AQUÍ
        this.repository = repository;
        this.ahorrosRepo = ahorrosRepo;
        this.corrienteRepo = corrienteRepo;
        this.tarjetaRepo = tarjetaRepo; // Ahora sí funcionará
    }

    @Override
    public void crearCuenta(String username, double saldoInicial, TypoCuenta tipo) {
        int siguienteId = repository.findAllCuentas().size() + 1;
        String prefijo = switch (tipo) {
            case AHORROS -> "CA-";
            case CORRIENTE -> "CC-";
            case TARJETA_CREDITO -> "TC-";
        };
        String numCuenta = prefijo + String.format("%03d", siguienteId);

        switch (tipo) {
            case AHORROS -> {
                CuentaAhorros ahorro = new CuentaAhorros(numCuenta, saldoInicial, username);
                ahorrosRepo.saveCuentaA(ahorro); // Guarda en repositorio de ahorros
                repository.saveCuenta(ahorro);   // Guarda en repositorio general
            }
            case CORRIENTE -> {
                double sobregiro = saldoInicial * 0.2;
                CuentaCorriente corriente = new CuentaCorriente(numCuenta, saldoInicial, username, sobregiro);
                corrienteRepo.saveCuentaC(corriente); // Guarda en repositorio corriente
                repository.saveCuenta(corriente);
            }
            case TARJETA_CREDITO -> {
                TarjetaCredito tarjeta = new TarjetaCredito(numCuenta, 4000000.0, username);
                tarjetaRepo.saveTarjeta(tarjeta); // Guarda en repositorio tarjetas
                repository.saveCuenta(tarjeta);
            }
        }
    }

    @Override
    public Cuenta obtenerCuentaPorTipo(String username, TypoCuenta tipo) {
        return switch (tipo) {
            case AHORROS -> ahorrosRepo.fidByPropietario(username);
            case CORRIENTE -> corrienteRepo.findbypropietario(username);
            case TARJETA_CREDITO -> tarjetaRepo.findByPropietario(username);
        };
    }

    @Override
    public void consignar(String identificador, double monto) {
        Cuenta c = repository.findCuentaBynumCuenta(identificador);
        if (c != null) {
            c.setSaldo(c.getSaldo() + monto);
            c.getMovimientos().add("Consignación: +$" + monto);
        }
    }

    @Override
    public void retirar(String identificador, double monto) {
        Cuenta c = repository.findCuentaBynumCuenta(identificador);
        if (c != null && c.getSaldo() >= monto) {
            c.setSaldo(c.getSaldo() - monto);
            c.getMovimientos().add("Retiro: -$" + monto);
        }
    }

    @Override
    public Cuenta obtenerCuenta(String identificador) {
        return repository.findCuentaBynumCuenta(identificador);
    }
    @Override
    public List<Cuenta> listarTodasLasCuentas() {
        return repository.findAllCuentas();
    }
}
