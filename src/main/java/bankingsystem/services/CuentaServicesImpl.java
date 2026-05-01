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
        Cuenta existente = obtenerCuentaPorTipo(username, tipo);
        if (existente != null) {
            throw new RuntimeException("Ya existe una cuenta de tipo " + tipo + " para este usuario.");
        }

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
                ahorro.setTipo(TypoCuenta.AHORROS);
                ahorrosRepo.saveCuentaA(ahorro); // Guarda en repositorio de ahorros
                repository.saveCuenta(ahorro);   // Guarda en repositorio general
            }
            case CORRIENTE -> {
                double sobregiro = saldoInicial * 0.2;
                CuentaCorriente corriente = new CuentaCorriente(numCuenta, saldoInicial, username, sobregiro);
                corriente.setTipo(TypoCuenta.CORRIENTE);
                corrienteRepo.saveCuentaC(corriente); // Guarda en repositorio corriente
                repository.saveCuenta(corriente);
            }
            case TARJETA_CREDITO -> {
                TarjetaCredito tarjeta = new TarjetaCredito(numCuenta, 4000000.0, username);
                tarjeta.setTipo(TypoCuenta.TARJETA_CREDITO);
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
    public void consignar(String username, TypoCuenta tipo, double monto) {
        Cuenta c = obtenerCuenta(username, tipo);
        if (c == null) {
            throw new RuntimeException("No se encontró la cuenta para el tipo seleccionado.");
        }
        if (monto <= 0) {
            throw new RuntimeException("El monto a consignar debe ser mayor a cero.");
        }
        c.setSaldo(c.getSaldo() + monto);
        c.getMovimientos().add("Consignación: +$" + monto);
    }

    @Override
    public void retirar(String username, TypoCuenta tipo, double monto) {
        Cuenta c = obtenerCuenta(username, tipo);
        if (c == null) {
            throw new RuntimeException("No se encontró la cuenta para el tipo seleccionado.");
        }
        if (monto <= 0) {
            throw new RuntimeException("El monto a retirar debe ser mayor a cero.");
        }
        if (c.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente para realizar el retiro.");
        }
        c.setSaldo(c.getSaldo() - monto);
        c.getMovimientos().add("Retiro: -$" + monto);
    }

    @Override
    public void transferirEntrePropias(String username, TypoCuenta tipoOrigen, TypoCuenta tipoDestino, double monto) {
        if (tipoOrigen == tipoDestino) {
            throw new RuntimeException("No puedes transferir al mismo tipo de cuenta propia.");
        }

        Cuenta origen = obtenerCuenta(username, tipoOrigen);
        Cuenta destino = obtenerCuenta(username, tipoDestino);

        if (destino == null) {
            throw new RuntimeException("No tienes cuenta destino del tipo seleccionado.");
        }

        ejecutarTransferencia(origen, destino, monto, "Transferencia propia");
    }

    @Override
    public void transferirATercero(String usernameOrigen, TypoCuenta tipoOrigen, String numeroCuentaDestino, double monto) {
        Cuenta origen = obtenerCuenta(usernameOrigen, tipoOrigen);
        Cuenta destino = repository.findByNumeroCuenta(numeroCuentaDestino);

        if (destino == null) {
            throw new RuntimeException("La cuenta destino no existe.");
        }

        if (origen != null && origen.getNumeroCuenta().equalsIgnoreCase(destino.getNumeroCuenta())) {
            throw new RuntimeException("No puedes transferir a la misma cuenta de origen.");
        }

        if (destino.getPropietario().equalsIgnoreCase(usernameOrigen) && destino.getTipo() == tipoOrigen) {
            throw new RuntimeException("No puedes transferir al mismo tipo de cuenta propia.");
        }

        ejecutarTransferencia(origen, destino, monto, "Transferencia a tercero");
    }

    private void ejecutarTransferencia(Cuenta origen, Cuenta destino, double monto, String concepto) {
        if (origen == null) {
            throw new RuntimeException("No se encontró la cuenta origen para el tipo seleccionado.");
        }
        if (monto <= 0) {
            throw new RuntimeException("El monto a transferir debe ser mayor a cero.");
        }

        validarCuentaTransferible(origen, "origen");
        validarCuentaTransferible(destino, "destino");

        if (origen.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente para realizar la transferencia.");
        }

        origen.setSaldo(origen.getSaldo() - monto);
        destino.setSaldo(destino.getSaldo() + monto);

        String debito = String.format("%s enviada a %s (%s): -$%,.2f",
                concepto,
                destino.getNumeroCuenta(),
                destino.getPropietario(),
                monto);
        String credito = String.format("%s recibida desde %s (%s): +$%,.2f",
                concepto,
                origen.getNumeroCuenta(),
                origen.getPropietario(),
                monto);

        origen.getMovimientos().add(debito);
        destino.getMovimientos().add(credito);
    }

    private void validarCuentaTransferible(Cuenta cuenta, String rol) {
        if (cuenta.getTipo() == TypoCuenta.TARJETA_CREDITO) {
            throw new RuntimeException("No se permiten transferencias con tarjeta de crédito como " + rol + ".");
        }
    }

    @Override
    public Cuenta obtenerCuenta(String username, TypoCuenta tipo) {
        return repository.findByPropietarioAndTipo(username, tipo);
    }
    @Override
    public List<Cuenta> listarTodasLasCuentas() {
        return repository.findAllCuentas();
    }
}
