package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Repositorio.ICuenta;
import com.Cesde.BankingSystem.Repositorio.IMovimiento;
import com.Cesde.BankingSystem.Repositorio.ITarjetaCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

// Servicio de MCuenta (la superclase de la herencia JOINED).
// Maneja la logica comun a cualquier tipo de cuenta. Lo que devuelve ICuenta
// ya es el objeto concreto (MCuentaAhorro o MCuentaCorriente), por eso aqui se
// puede consultar cualquier cuenta sin saber de que tipo es.
// La logica propia de cada tipo (intereses, sobregiro) va en SCuentaAhorro y
// SCuentaCorriente.
@Service
public class SCuenta {

    @Autowired
    ICuenta iCuenta;

    @Autowired
    IMovimiento iMovimiento;

    @Autowired
    ITarjetaCredito iTarjetaCredito;

    // =====================  ESCRITURA  =====================

    // Adicionar una cuenta. Recibe una cuenta concreta (MCuentaAhorro o
    // MCuentaCorriente) porque MCuenta es abstracta: no existe "new MCuenta()".
    public MCuenta adicionarCuenta(MCuenta cuenta) {
        validarCuentaNueva(cuenta);
        cuenta.setEstado(true);
        cuenta.setCreatedat(LocalDateTime.now());
        return iCuenta.save(cuenta);
    }

    // Modificar una cuenta: solo propietario y estado.
    // El saldo NO se modifica aqui a proposito: el saldo solo cambia por medio
    // de un movimiento (SMovimiento) para que siempre quede el registro.
    public MCuenta modificarCuenta(int id, MCuenta datos) {
        MCuenta cuenta = buscarCuentaPorId(id);
        if (datos.getPropietario() != null && !datos.getPropietario().isBlank()) {
            cuenta.setPropietario(datos.getPropietario().trim());
        }
        if (datos.getEstado() != null) {
            cuenta.setEstado(datos.getEstado());
        }
        return iCuenta.save(cuenta);
    }

    // Anular una cuenta (eliminacion logica): el registro se conserva en la BD
    // pero queda inactivo. Es lo normal en un banco, porque se debe guardar el
    // historial.
    public MCuenta anularCuenta(int id) {
        MCuenta cuenta = buscarCuentaPorId(id);
        cuenta.setEstado(false);
        return iCuenta.save(cuenta);
    }

    // Eliminar una cuenta (eliminacion fisica): borra el registro de la BD.
    // Solo se permite si la cuenta no tiene movimientos ni tarjetas, porque esos
    // registros tienen llave foranea hacia la cuenta (la BD no dejaria borrarla).
    public void eliminarCuenta(int id) {
        MCuenta cuenta = buscarCuentaPorId(id);
        if (!iMovimiento.findByCuenta_IdOrderByFechaDesc(id).isEmpty()) {
            throw error(HttpStatus.CONFLICT, "La cuenta tiene movimientos: no se puede eliminar, solo anular.");
        }
        if (!iTarjetaCredito.findByCuenta_Id(id).isEmpty()) {
            throw error(HttpStatus.CONFLICT, "La cuenta tiene tarjetas asociadas: no se puede eliminar.");
        }
        iCuenta.delete(cuenta);
    }

    // =====================  CONSULTAS  =====================

    // Consulta general
    public List<MCuenta> listarCuentas() {
        return iCuenta.findAll();
    }

    // Consulta por llave primaria
    public MCuenta buscarCuentaPorId(int id) {
        return iCuenta.findById(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una cuenta con id " + id + "."));
    }

    // Consulta por numero de cuenta
    public MCuenta buscarCuentaPorNumero(String numerocuenta) {
        MCuenta cuenta = iCuenta.findByNumerocuenta(numerocuenta);
        if (cuenta == null) {
            throw error(HttpStatus.NOT_FOUND, "No existe la cuenta " + numerocuenta + ".");
        }
        return cuenta;
    }

    // Consulta de las cuentas de una persona
    public List<MCuenta> listarCuentasPorPersona(int idperson) {
        return iCuenta.findByPersona_Idperson(idperson);
    }

    // Consulta de cuentas activas o inactivas
    public List<MCuenta> listarCuentasPorEstado(Boolean estado) {
        return iCuenta.findByEstado(estado);
    }

    // =====================  VALIDACIONES  =====================

    // Validacion comun para crear cualquier tipo de cuenta. Es public porque
    // SCuentaAhorro y SCuentaCorriente tambien la usan (reutilizacion).
    public void validarCuentaNueva(MCuenta cuenta) {
        String numero = cuenta.getNumerocuenta();
        if (numero == null || numero.isBlank()) {
            throw error(HttpStatus.BAD_REQUEST, "El numero de cuenta es obligatorio.");
        }
        if (iCuenta.existsByNumerocuenta(numero)) {
            throw error(HttpStatus.CONFLICT, "Ya existe una cuenta con el numero " + numero + ".");
        }
        if (cuenta.getSaldo() == null || cuenta.getSaldo() < 0) {
            throw error(HttpStatus.BAD_REQUEST, "El saldo inicial debe ser mayor o igual a 0.");
        }
        if (cuenta.getPersona() == null) {
            throw error(HttpStatus.BAD_REQUEST, "La cuenta debe pertenecer a una persona.");
        }
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}
