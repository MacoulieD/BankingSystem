package bankingsystem.services;

import bankingsystem.Persistence.repository.CuentaRepository;
import bankingsystem.Persistence.repository.MovimientoRepository;
import bankingsystem.domain.*;
import bankingsystem.domain.enums.TipoMovimiento;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.services.input.CuentaServices;
import bankingsystem.services.outputport.CuentaAhorrosPersistencePort;
import bankingsystem.services.outputport.CuentaCorrientePersistencePort;
import bankingsystem.services.outputport.TarjetaCreditoPersistencePort;
import bankingsystem.services.outputport.MovimientoPersistencePort; // ✅ CORREGIDO: Importamos el nuevo puerto

import java.util.List;

public class CuentaServicesImpl implements CuentaServices {

    private final CuentaRepository repository;
    private final CuentaAhorrosPersistencePort ahorrosRepo;
    private final CuentaCorrientePersistencePort corrienteRepo;
    private final TarjetaCreditoPersistencePort tarjetaRepo; // ✅ CORREGIDO: Usamos la interfaz del puerto
    private final MovimientoRepository movimientoRepo;
    private final MovimientoPersistencePort movimientoPersistencePort; // ✅ Puerto de salida para BD

    // ✅ CORREGIDO: Constructor adaptado 100% a la Arquitectura Hexagonal y sus puertos de salida
    public CuentaServicesImpl(CuentaRepository repository,
                              CuentaAhorrosPersistencePort ahorrosRepo,
                              CuentaCorrientePersistencePort corrienteRepo,
                              TarjetaCreditoPersistencePort tarjetaRepo, // ✅ Inyección por interfaz
                              MovimientoRepository movimientoRepo,
                              MovimientoPersistencePort movimientoPersistencePort) {
        this.repository = repository;
        this.ahorrosRepo = ahorrosRepo;
        this.corrienteRepo = corrienteRepo;
        this.tarjetaRepo = tarjetaRepo;
        this.movimientoRepo = movimientoRepo;
        this.movimientoPersistencePort = movimientoPersistencePort;
    }

    @Override
    public void crearCuenta(String username, double saldoInicial, TypoCuenta tipo) {
        Cuenta existente = obtenerCuentaPorTipo(username, tipo);
        if (existente != null) {
            throw new RuntimeException("Ya existe una cuenta de tipo " + tipo + " para este usuario.");
        }

        int siguienteId = ahorrosRepo.countCuentas() + 1;
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
                ahorrosRepo.saveCuentaAhorros(ahorro);
                repository.saveCuenta(ahorro);
            }
            case CORRIENTE -> {
                double sobregiro = saldoInicial * 0.2;
                CuentaCorriente corriente = new CuentaCorriente(numCuenta, saldoInicial, username, sobregiro);
                corriente.setTipo(TypoCuenta.CORRIENTE);
                corrienteRepo.saveCuentaC(corriente);
                repository.saveCuenta(corriente);
            }
            case TARJETA_CREDITO -> {
                TarjetaCredito tarjeta = new TarjetaCredito(numCuenta, username, 4000000.0);
                tarjeta.setTipo(TypoCuenta.TARJETA_CREDITO);
                // saldo (deuda) empieza en 0, asignado por el constructor
                tarjetaRepo.saveTarjeta(tarjeta);
                repository.saveCuenta(tarjeta);
            }
        }
    }

    @Override
    public Cuenta obtenerCuentaPorTipo(String username, TypoCuenta tipo) {
        return switch (tipo) {
            case AHORROS -> ahorrosRepo.findByPropietario(username);
            case CORRIENTE -> corrienteRepo.findbypropietario(username); // Asegúrate de que coincida con el nombre exacto de tu puerto (findbypropietario / findByPropietario)
            case TARJETA_CREDITO -> {
                // ✅ CORREGIDO: Buscamos las tarjetas asociadas al propietario y devolvemos la primera
                List<TarjetaCredito> tarjetas = tarjetaRepo.findByPropietario(username);
                yield (tarjetas != null && !tarjetas.isEmpty()) ? tarjetas.get(0) : null;
            }
        };
    }

    @Override
    public void consignar(String username, TypoCuenta tipo, double monto) {
        Cuenta c = obtenerCuentaPorTipo(username, tipo);
        if (c == null) {
            throw new RuntimeException("No se encontró la cuenta para el tipo seleccionado.");
        }
        if (monto <= 0) {
            throw new RuntimeException("El monto a consignar debe ser mayor a cero.");
        }
        c.setSaldo(c.getSaldo() + monto);

        Movimiento movC = new Movimiento(c.getMovimientos().size() + 1, TipoMovimiento.CONSIGNACION, monto, c.getSaldo(), String.format("Consignación: +$%,.2f", monto));
        c.getMovimientos().add(movC);
        movimientoRepo.save(movC);
        movimientoPersistencePort.saveMovimiento(c.getNumeroCuenta(), movC); // ✅ Persiste en BD

        actualizarPersistenciaCuenta(c);
        repository.saveCuenta(c);
    }

    @Override
    public void retirar(String username, TypoCuenta tipo, double monto) {
        Cuenta c = obtenerCuentaPorTipo(username, tipo);
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

        Movimiento movR = new Movimiento(c.getMovimientos().size() + 1, TipoMovimiento.RETIRO, monto, c.getSaldo(), String.format("Retiro: -$%,.2f", monto));
        c.getMovimientos().add(movR);
        movimientoRepo.save(movR);
        movimientoPersistencePort.saveMovimiento(c.getNumeroCuenta(), movR); // ✅ Persiste en BD

        actualizarPersistenciaCuenta(c);
        repository.saveCuenta(c);
    }

    @Override
    public void transferirEntrePropias(String username, TypoCuenta tipoOrigen, TypoCuenta tipoDestino, double monto) {
        if (tipoOrigen == tipoDestino) {
            throw new RuntimeException("No puedes transferir al mismo tipo de cuenta propia.");
        }

        Cuenta origen = obtenerCuentaPorTipo(username, tipoOrigen);
        Cuenta destino = obtenerCuentaPorTipo(username, tipoDestino);

        if (destino == null) {
            throw new RuntimeException("No tienes cuenta destino del tipo seleccionado.");
        }

        ejecutarTransferencia(origen, destino, monto, "Transferencia propia");
    }

    @Override
    public void transferirATercero(String usernameOrigen, TypoCuenta tipoOrigen, String numeroCuentaDestino, double monto) {
        Cuenta origen = obtenerCuentaPorTipo(usernameOrigen, tipoOrigen);
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

        String debito = String.format("%s enviada a %s (%s): -$%,.2f", concepto, destino.getNumeroCuenta(), destino.getPropietario(), monto);
        String credito = String.format("%s recibida desde %s (%s): +$%,.2f", concepto, origen.getNumeroCuenta(), origen.getPropietario(), monto);

        Movimiento movOut = new Movimiento(origen.getMovimientos().size() + 1, TipoMovimiento.TRANSFERENCIA_OUT, monto, origen.getSaldo(), debito);
        origen.getMovimientos().add(movOut);
        movimientoRepo.save(movOut);
        movimientoPersistencePort.saveMovimiento(origen.getNumeroCuenta(), movOut); // ✅ Persiste en BD

        Movimiento movIn = new Movimiento(destino.getMovimientos().size() + 1, TipoMovimiento.TRANSFERENCIA_IN, monto, destino.getSaldo(), credito);
        destino.getMovimientos().add(movIn);
        movimientoRepo.save(movIn);
        movimientoPersistencePort.saveMovimiento(destino.getNumeroCuenta(), movIn); // ✅ Persiste en BD

        actualizarPersistenciaCuenta(origen);
        actualizarPersistenciaCuenta(destino);
        repository.saveCuenta(origen);
        repository.saveCuenta(destino);
    }

    private void validarCuentaTransferible(Cuenta cuenta, String rol) {
        if (cuenta.getTipo() == TypoCuenta.TARJETA_CREDITO) {
            throw new RuntimeException("No se permiten transferencias con tarjeta de crédito como " + rol + ".");
        }
    }

    private void actualizarPersistenciaCuenta(Cuenta cuenta) {
        switch (cuenta.getTipo()) {
            case AHORROS -> ahorrosRepo.saveCuentaAhorros((CuentaAhorros) cuenta);
            case CORRIENTE -> corrienteRepo.saveCuentaC((CuentaCorriente) cuenta);
            case TARJETA_CREDITO -> tarjetaRepo.saveTarjeta((TarjetaCredito) cuenta); // ✅ Vinculado al nuevo adaptador sin Cast raros
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

    // ── Historia de usuario: consultar movimientos desde la BD ─────────────────
    @Override
    public List<Movimiento> obtenerMovimientos(String username, TypoCuenta tipo) {
        Cuenta cuenta = obtenerCuentaPorTipo(username, tipo);
        if (cuenta == null) {
            throw new RuntimeException("No se encontró la cuenta de tipo " + tipo + " para el usuario.");
        }
        return movimientoPersistencePort.findByNumeroCuenta(cuenta.getNumeroCuenta());
    }
}