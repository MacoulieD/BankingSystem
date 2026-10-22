package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MCuentaCorriente;
import com.Cesde.BankingSystem.Modelo.MMovimiento;
import com.Cesde.BankingSystem.Repositorio.ICuenta;
import com.Cesde.BankingSystem.Repositorio.IMovimiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Cesde.BankingSystem.Excepcion.ExcepcionNegocio;

import java.time.LocalDateTime;
import java.util.List;

// Servicio de MMovimiento: registra depositos, retiros y transferencias y
// actualiza el saldo de la cuenta en la misma operacion.
@Service
public class SMovimiento {

    // tipos de movimiento que se guardan en la columna tipomovimiento
    public static final String DEPOSITO = "deposit";
    public static final String RETIRO = "withdrawal";
    public static final String TRANSFERENCIA_SALIDA = "transfer-out";
    public static final String TRANSFERENCIA_ENTRADA = "transfer-in";
    public static final String INTERES = "interest";

    @Autowired
    IMovimiento iMovimiento;

    @Autowired
    ICuenta iCuenta;

    // =====================  ESCRITURA  =====================

    // Adicionar un deposito (consignacion): suma al saldo
    @Transactional(rollbackFor = Exception.class)
    public MMovimiento depositar(String numerocuenta, Double monto, String descripcion) throws Exception {
        try {
            validarMonto(monto);
            MCuenta cuenta = buscarCuentaActiva(numerocuenta);
            return registrarMovimiento(cuenta, DEPOSITO, monto, descripcion == null ? "Consignación" : descripcion);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Adicionar un retiro: resta al saldo si hay fondos suficientes
    @Transactional(rollbackFor = Exception.class)
    public MMovimiento retirar(String numerocuenta, Double monto, String descripcion) throws Exception {
        try {
            validarMonto(monto);
            MCuenta cuenta = buscarCuentaActiva(numerocuenta);
            validarFondos(cuenta, monto);
            return registrarMovimiento(cuenta, RETIRO, -monto, descripcion == null ? "Retiro" : descripcion);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Transferencia entre dos cuentas: genera DOS movimientos (salida y entrada).
    // @Transactional garantiza que se hagan los dos o ninguno: si la segunda
    // parte falla, la primera se deshace (rollback) y el dinero no se pierde.
    @Transactional(rollbackFor = Exception.class)
    public List<MMovimiento> transferir(String cuentaOrigen, String cuentaDestino, Double monto) throws Exception {
        try {
            validarMonto(monto);
            if (cuentaOrigen.equals(cuentaDestino)) {
                throw error(HttpStatus.BAD_REQUEST, "La cuenta de origen y la de destino deben ser diferentes.");
            }
            MCuenta origen = buscarCuentaActiva(cuentaOrigen);
            MCuenta destino = buscarCuentaActiva(cuentaDestino);
            validarFondos(origen, monto);
            MMovimiento salida = registrarMovimiento(origen, TRANSFERENCIA_SALIDA, -monto,
                    "Transferencia a " + destino.getNumerocuenta());
            MMovimiento entrada = registrarMovimiento(destino, TRANSFERENCIA_ENTRADA, monto,
                    "Transferencia de " + origen.getNumerocuenta());
            return List.of(salida, entrada);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Metodo central: crea el movimiento y actualiza el saldo de la cuenta.
    // variacion > 0 entra dinero, variacion < 0 sale dinero.
    // Es public porque SCuentaAhorro lo usa para registrar los intereses.
    public MMovimiento registrarMovimiento(MCuenta cuenta, String tipo, double variacion, String descripcion) throws Exception {
        try {
            double saldoAnterior = cuenta.getSaldo();
            double saldoPosterior = saldoAnterior + variacion;

            cuenta.setSaldo(saldoPosterior);
            iCuenta.save(cuenta);

            MMovimiento movimiento = new MMovimiento();
            movimiento.setCuenta(cuenta);
            movimiento.setNumerocuenta(cuenta.getNumerocuenta());
            movimiento.setTipomovimiento(tipo);
            movimiento.setMonto(Math.abs(variacion));
            movimiento.setSaldoanterior(saldoAnterior);
            movimiento.setSaldoposterior(saldoPosterior);
            movimiento.setDescripcion(descripcion);
            movimiento.setFecha(LocalDateTime.now());
            return iMovimiento.save(movimiento);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Modificar un movimiento: solo la descripcion. El monto, la fecha y los
    // saldos no se cambian porque alterarian el historial y el saldo de la cuenta.
    public MMovimiento modificarMovimiento(int id, String descripcion) throws Exception {
        try {
            MMovimiento movimiento = buscarMovimientoPorId(id);
            if (descripcion == null || descripcion.isBlank()) {
                throw error(HttpStatus.BAD_REQUEST, "La descripción no puede estar vacía.");
            }
            movimiento.setDescripcion(descripcion.trim());
            return iMovimiento.save(movimiento);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Eliminar un movimiento (eliminacion fisica). Ojo: no devuelve el dinero,
    // solo borra el registro. En un banco real no se borran movimientos: se
    // hace un movimiento contrario (reverso). Se deja para administracion.
    public void eliminarMovimiento(int id) throws Exception {
        try {
            if (!iMovimiento.existsById(id)) {
                throw error(HttpStatus.NOT_FOUND, "No existe un movimiento con id " + id + ".");
            }
            iMovimiento.deleteById(id);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // =====================  CONSULTAS  =====================

    // Consulta general
    public List<MMovimiento> listarMovimientos() throws Exception {
        try {
            return iMovimiento.findAll();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta por llave primaria
    public MMovimiento buscarMovimientoPorId(int id) throws Exception {
        try {
            return iMovimiento.findById(id)
                    .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe un movimiento con id " + id + "."));
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Historial (extracto) de una cuenta por su numero
    public List<MMovimiento> listarPorNumeroCuenta(String numerocuenta) throws Exception {
        try {
            return iMovimiento.findByNumerocuenta(numerocuenta);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Historial de una cuenta por su id, del mas reciente al mas antiguo
    public List<MMovimiento> listarPorCuenta(int idcuenta) throws Exception {
        try {
            return iMovimiento.findByCuenta_IdOrderByFechaDesc(idcuenta);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Movimientos entre dos fechas
    public List<MMovimiento> listarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) throws Exception {
        try {
            if (desde == null || hasta == null || desde.isAfter(hasta)) {
                throw error(HttpStatus.BAD_REQUEST, "El rango de fechas no es válido.");
            }
            return iMovimiento.findByFechaBetween(desde, hasta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // =====================  VALIDACIONES  =====================

    private MCuenta buscarCuentaActiva(String numerocuenta) throws ExcepcionNegocio {
        MCuenta cuenta = iCuenta.findByNumerocuenta(numerocuenta);
        if (cuenta == null) {
            throw error(HttpStatus.NOT_FOUND, "No existe la cuenta " + numerocuenta + ".");
        }
        if (!Boolean.TRUE.equals(cuenta.getEstado())) {
            throw error(HttpStatus.CONFLICT, "La cuenta " + numerocuenta + " está inactiva.");
        }
        return cuenta;
    }

    private void validarMonto(Double monto) throws ExcepcionNegocio {
        if (monto == null || monto <= 0) {
            throw error(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a 0.");
        }
    }

    // Fondos disponibles = saldo, y en la cuenta corriente ademas el sobregiro.
    // Aqui se ve el polimorfismo: segun el tipo real de la cuenta cambia la regla.
    private void validarFondos(MCuenta cuenta, double monto) throws ExcepcionNegocio {
        double disponible = cuenta.getSaldo();
        if (cuenta instanceof MCuentaCorriente corriente) {
            disponible += corriente.getSobregiro();
        }
        if (monto > disponible) {
            throw error(HttpStatus.CONFLICT, "Fondos insuficientes. Disponible: " + disponible + ".");
        }
    }

    private static ExcepcionNegocio error(HttpStatus estado, String mensaje) {
        return new ExcepcionNegocio(estado, mensaje);
    }
}
