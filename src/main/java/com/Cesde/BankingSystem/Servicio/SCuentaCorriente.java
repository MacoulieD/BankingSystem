package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Modelo.MCuentaCorriente;
import com.Cesde.BankingSystem.Repositorio.ICuentaCorriente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.Cesde.BankingSystem.Excepcion.ExcepcionNegocio;

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
    public MCuentaCorriente adicionarCuentaCorriente(MCuentaCorriente cuenta) throws Exception {
        try {
            sCuenta.validarCuentaNueva(cuenta);
            validarSobregiro(cuenta.getSobregiro());
            cuenta.setEstado(true);
            cuenta.setCreatedat(LocalDateTime.now());
            return iCuentaCorriente.save(cuenta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Modificar: propietario, estado y sobregiro (el saldo no, ver SCuenta)
    public MCuentaCorriente modificarCuentaCorriente(int id, MCuentaCorriente datos) throws Exception {
        try {
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
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Eliminar: misma regla que cualquier cuenta (sin movimientos ni tarjetas)
    public void eliminarCuentaCorriente(int id) throws Exception {
        try {
            buscarCuentaCorrientePorId(id);
            sCuenta.eliminarCuenta(id);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Logica propia: saldo disponible = saldo + sobregiro
    public double consultarSaldoDisponible(int id) throws Exception {
        try {
            MCuentaCorriente cuenta = buscarCuentaCorrientePorId(id);
            return cuenta.getSaldo() + cuenta.getSobregiro();
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
    public List<MCuentaCorriente> listarCuentasCorriente() throws Exception {
        try {
            return iCuentaCorriente.findAll();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta por llave primaria
    public MCuentaCorriente buscarCuentaCorrientePorId(int id) throws Exception {
        try {
            return iCuentaCorriente.findById(id)
                    .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una cuenta corriente con id " + id + "."));
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta por numero de cuenta
    public MCuentaCorriente buscarCuentaCorrientePorNumero(String numerocuenta) throws Exception {
        try {
            MCuentaCorriente cuenta = iCuentaCorriente.findByNumerocuenta(numerocuenta);
            if (cuenta == null) {
                throw error(HttpStatus.NOT_FOUND, "No existe la cuenta corriente " + numerocuenta + ".");
            }
            return cuenta;
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Cuentas corrientes de una persona
    public List<MCuentaCorriente> listarPorPersona(int idperson) throws Exception {
        try {
            return iCuentaCorriente.findByPersona_Idperson(idperson);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Cuentas corrientes activas o inactivas
    public List<MCuentaCorriente> listarPorEstado(Boolean estado) throws Exception {
        try {
            return iCuentaCorriente.findByEstado(estado);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // =====================  VALIDACIONES  =====================

    private void validarSobregiro(Double sobregiro) throws ExcepcionNegocio {
        if (sobregiro == null || sobregiro < 0) {
            throw error(HttpStatus.BAD_REQUEST, "El sobregiro debe ser mayor o igual a 0.");
        }
    }

    private static ExcepcionNegocio error(HttpStatus estado, String mensaje) {
        return new ExcepcionNegocio(estado, mensaje);
    }
}
