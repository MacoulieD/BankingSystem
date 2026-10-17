package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Modelo.MCuentaCorriente;
import com.Cesde.BankingSystem.Repositorio.ICuentaCorriente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

// Servicio de MCuentaCorriente (subtipo de MCuenta).
// Reutiliza SCuenta para lo comun y agrega lo propio de la cuenta corriente:
// el sobregiro (dinero que el banco presta cuando el saldo no alcanza).
@Service
public class SCuentaCorriente {

    @Autowired
    ICuentaCorriente iCuentaCorriente;

    @Autowired
    SCuenta sCuenta;

    // =====================  ESCRITURA  =====================

    // Adicionar una cuenta corriente
    public MCuentaCorriente adicionarCuentaCorriente(MCuentaCorriente cuenta) {
        sCuenta.validarCuentaNueva(cuenta);
        validarSobregiro(cuenta.getSobregiro());
        cuenta.setEstado(true);
        cuenta.setCreatedat(LocalDateTime.now());
        return iCuentaCorriente.save(cuenta);
    }

    // Modificar: propietario, estado y sobregiro (el saldo no, ver SCuenta)
    public MCuentaCorriente modificarCuentaCorriente(int id, MCuentaCorriente datos) {
        MCuentaCorriente cuenta = buscarCuentaCorrientePorId(id);
        if (datos.getPropietario() != null && !datos.getPropietario().isBlank()) {
            cuenta.setPropietario(datos.getPropietario().trim());
        }
        if (datos.getEstado() != null) {
            cuenta.setEstado(datos.getEstado());
        }
        if (datos.getSobregiro() != null) {
            validarSobregiro(datos.getSobregiro());
            // si la cuenta ya esta usando sobregiro (saldo negativo), el nuevo
            // limite no puede quedar por debajo de lo que ya debe
            if (cuenta.getSaldo() < 0 && -cuenta.getSaldo() > datos.getSobregiro()) {
                throw error(HttpStatus.CONFLICT, "El sobregiro no puede ser menor a lo que la cuenta ya está usando.");
            }
            cuenta.setSobregiro(datos.getSobregiro());
        }
        return iCuentaCorriente.save(cuenta);
    }

    // Eliminar: misma regla que cualquier cuenta (sin movimientos ni tarjetas)
    public void eliminarCuentaCorriente(int id) {
        buscarCuentaCorrientePorId(id);
        sCuenta.eliminarCuenta(id);
    }

    // Logica propia: saldo disponible = saldo + sobregiro
    public double consultarSaldoDisponible(int id) {
        MCuentaCorriente cuenta = buscarCuentaCorrientePorId(id);
        return cuenta.getSaldo() + cuenta.getSobregiro();
    }

    // =====================  CONSULTAS  =====================

    // Consulta general
    public List<MCuentaCorriente> listarCuentasCorriente() {
        return iCuentaCorriente.findAll();
    }

    // Consulta por llave primaria
    public MCuentaCorriente buscarCuentaCorrientePorId(int id) {
        return iCuentaCorriente.findById(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una cuenta corriente con id " + id + "."));
    }

    // Consulta por numero de cuenta
    public MCuentaCorriente buscarCuentaCorrientePorNumero(String numerocuenta) {
        MCuentaCorriente cuenta = iCuentaCorriente.findByNumerocuenta(numerocuenta);
        if (cuenta == null) {
            throw error(HttpStatus.NOT_FOUND, "No existe la cuenta corriente " + numerocuenta + ".");
        }
        return cuenta;
    }

    // Cuentas corrientes de una persona
    public List<MCuentaCorriente> listarPorPersona(int idperson) {
        return iCuentaCorriente.findByPersona_Idperson(idperson);
    }

    // Cuentas corrientes activas o inactivas
    public List<MCuentaCorriente> listarPorEstado(Boolean estado) {
        return iCuentaCorriente.findByEstado(estado);
    }

    // =====================  VALIDACIONES  =====================

    private void validarSobregiro(Double sobregiro) {
        if (sobregiro == null || sobregiro < 0) {
            throw error(HttpStatus.BAD_REQUEST, "El sobregiro debe ser mayor o igual a 0.");
        }
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}
