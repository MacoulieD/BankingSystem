package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Modelo.MCuentaAhorro;
import com.Cesde.BankingSystem.Modelo.MMovimiento;
import com.Cesde.BankingSystem.Repositorio.ICuentaAhorro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

// Servicio de MCuentaAhorro (subtipo de MCuenta).
// Reutiliza SCuenta para lo comun (validar numero, eliminar) y agrega lo propio
// de la cuenta de ahorros: la tasa de interes.
@Service
public class SCuentaAhorro {

    @Autowired
    ICuentaAhorro iCuentaAhorro;

    @Autowired
    SCuenta sCuenta;

    @Autowired
    SMovimiento sMovimiento;

    // =====================  ESCRITURA  =====================

    // Adicionar una cuenta de ahorros
    public MCuentaAhorro adicionarCuentaAhorro(MCuentaAhorro cuenta) {
        sCuenta.validarCuentaNueva(cuenta);
        validarTasa(cuenta.getTasainteres());
        cuenta.setEstado(true);
        cuenta.setCreatedat(LocalDateTime.now());
        return iCuentaAhorro.save(cuenta);
    }

    // Modificar: propietario, estado y tasa de interes (el saldo no, ver SCuenta)
    public MCuentaAhorro modificarCuentaAhorro(int id, MCuentaAhorro datos) {
        MCuentaAhorro cuenta = buscarCuentaAhorroPorId(id);
        if (datos.getPropietario() != null && !datos.getPropietario().isBlank()) {
            cuenta.setPropietario(datos.getPropietario().trim());
        }
        if (datos.getEstado() != null) {
            cuenta.setEstado(datos.getEstado());
        }
        if (datos.getTasainteres() != null) {
            validarTasa(datos.getTasainteres());
            cuenta.setTasainteres(datos.getTasainteres());
        }
        return iCuentaAhorro.save(cuenta);
    }

    // Eliminar: misma regla que cualquier cuenta (sin movimientos ni tarjetas)
    public void eliminarCuentaAhorro(int id) {
        buscarCuentaAhorroPorId(id);
        sCuenta.eliminarCuenta(id);
    }

    // Logica propia: liquidar intereses. saldo * tasa / 100, y queda
    // registrado como un movimiento de tipo "interest".
    @Transactional
    public MMovimiento aplicarIntereses(int id) {
        MCuentaAhorro cuenta = buscarCuentaAhorroPorId(id);
        if (!Boolean.TRUE.equals(cuenta.getEstado())) {
            throw error(HttpStatus.CONFLICT, "La cuenta está inactiva.");
        }
        double interes = cuenta.getSaldo() * cuenta.getTasainteres() / 100;
        if (interes <= 0) {
            throw error(HttpStatus.BAD_REQUEST, "La cuenta no genera intereses con el saldo actual.");
        }
        return sMovimiento.registrarMovimiento(cuenta, SMovimiento.INTERES, interes,
                "Intereses (" + cuenta.getTasainteres() + "%)");
    }

    // =====================  CONSULTAS  =====================

    // Consulta general
    public List<MCuentaAhorro> listarCuentasAhorro() {
        return iCuentaAhorro.findAll();
    }

    // Consulta por llave primaria
    public MCuentaAhorro buscarCuentaAhorroPorId(int id) {
        return iCuentaAhorro.findById(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una cuenta de ahorros con id " + id + "."));
    }

    // Consulta por numero de cuenta
    public MCuentaAhorro buscarCuentaAhorroPorNumero(String numerocuenta) {
        MCuentaAhorro cuenta = iCuentaAhorro.findByNumerocuenta(numerocuenta);
        if (cuenta == null) {
            throw error(HttpStatus.NOT_FOUND, "No existe la cuenta de ahorros " + numerocuenta + ".");
        }
        return cuenta;
    }

    // Cuentas de ahorros de una persona
    public List<MCuentaAhorro> listarPorPersona(int idperson) {
        return iCuentaAhorro.findByPersona_Idperson(idperson);
    }

    // Cuentas de ahorros activas o inactivas
    public List<MCuentaAhorro> listarPorEstado(Boolean estado) {
        return iCuentaAhorro.findByEstado(estado);
    }

    // =====================  VALIDACIONES  =====================

    private void validarTasa(Double tasa) {
        if (tasa == null || tasa < 0 || tasa > 100) {
            throw error(HttpStatus.BAD_REQUEST, "La tasa de interés debe estar entre 0 y 100.");
        }
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}
