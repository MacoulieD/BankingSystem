package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Dto.AbrirCuentaRequest;
import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.DestinoResponse;
import com.Cesde.BankingSystem.Mapper.CuentaMapper;
import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MPerson;
import com.Cesde.BankingSystem.Repositorio.ICuenta;
import com.Cesde.BankingSystem.Repositorio.IMovimiento;
import com.Cesde.BankingSystem.Repositorio.IPerson;
import com.Cesde.BankingSystem.Repositorio.ITarjetaCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Cesde.BankingSystem.Excepcion.ExcepcionNegocio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.Cesde.BankingSystem.Mapper.MapperUtils.texto;

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

    @Autowired
    IPerson iPerson;

    @Autowired
    SPerson sPerson;

    // nombres que muestra el frontend (ACCOUNT_TYPE_NAMES en bankAccounts.js)
    public static final Map<String, String> NOMBRES_TIPO = Map.of(
            CuentaMapper.TIPO_AHORRO, "Cuenta de Ahorros",
            CuentaMapper.TIPO_CORRIENTE, "Cuenta Corriente");

    // =====================  ESCRITURA  =====================

    // Adicionar una cuenta. Recibe una cuenta concreta (MCuentaAhorro o
    // MCuentaCorriente) porque MCuenta es abstracta: no existe "new MCuenta()".
    public MCuenta adicionarCuenta(MCuenta cuenta) throws Exception {
        try {
            validarCuentaNueva(cuenta);
            cuenta.setEstado(true);
            cuenta.setCreatedat(LocalDateTime.now());
            return iCuenta.save(cuenta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Modificar una cuenta: solo propietario y estado.
    // El saldo NO se modifica aqui a proposito: el saldo solo cambia por medio
    // de un movimiento (SMovimiento) para que siempre quede el registro.
    public MCuenta modificarCuenta(int id, MCuenta datos) throws Exception {
        try {
            MCuenta cuenta = buscarCuentaPorId(id);
            if (datos.getPropietario() != null && !datos.getPropietario().isBlank()) {
                cuenta.setPropietario(datos.getPropietario().trim());
            }
            if (datos.getEstado() != null) {
                cuenta.setEstado(datos.getEstado());
            }
            return iCuenta.save(cuenta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Anular una cuenta (eliminacion logica): el registro se conserva en la BD
    // pero queda inactivo. Es lo normal en un banco, porque se debe guardar el
    // historial.
    public MCuenta anularCuenta(int id) throws Exception {
        try {
            MCuenta cuenta = buscarCuentaPorId(id);
            cuenta.setEstado(false);
            return iCuenta.save(cuenta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Eliminar una cuenta (eliminacion fisica): borra el registro de la BD.
    // Solo se permite si la cuenta no tiene movimientos ni tarjetas, porque esos
    // registros tienen llave foranea hacia la cuenta (la BD no dejaria borrarla).
    public void eliminarCuenta(int id) throws Exception {
        try {
            MCuenta cuenta = buscarCuentaPorId(id);
            if (!iMovimiento.findByCuenta_IdOrderByFechaDesc(id).isEmpty()) {
                throw error(HttpStatus.CONFLICT, "La cuenta tiene movimientos: no se puede eliminar, solo anular.");
            }
            if (!iTarjetaCredito.findByCuenta_Id(id).isEmpty()) {
                throw error(HttpStatus.CONFLICT, "La cuenta tiene tarjetas asociadas: no se puede eliminar.");
            }
            iCuenta.delete(cuenta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // =====================  OPERACIONES DEL FRONTEND  =====================

    // Abrir otra cuenta desde el dashboard: maximo una de cada tipo
    // (ahorros y corriente) por usuario. Devuelve el usuario actualizado.
    @Transactional(rollbackFor = Exception.class)
    public CuentaResponse abrirCuenta(AbrirCuentaRequest datos) throws Exception {
        try {
            MPerson persona = sPerson.buscarPersonaActiva(datos.username());
            String tipo = texto(datos.accountType());
            if (!tipo.equals(CuentaMapper.TIPO_AHORRO) && !tipo.equals(CuentaMapper.TIPO_CORRIENTE)) {
                throw error(HttpStatus.BAD_REQUEST, "Tipo de cuenta no válido.");
            }
            double saldo = Validaciones.enteroNoNegativo(datos.balance(),
                    "El saldo inicial debe ser un número entero mayor o igual a 0.");
            boolean yaLaTiene = iCuenta.findByPersona_Idperson(persona.getIdperson()).stream()
                    .anyMatch(cuenta -> CuentaMapper.tipo(cuenta).equals(tipo));
            if (yaLaTiene) {
                throw error(HttpStatus.CONFLICT, "Ya tienes una " + NOMBRES_TIPO.get(tipo) + ".");
            }
            iCuenta.save(CuentaMapper.toEntity(tipo, (long) saldo, persona, sPerson.generarNumeroCuenta()));
            return sPerson.armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Destino de una transferencia a una cuenta MiPlata, buscado por numero de cuenta
    public DestinoResponse buscarDestino(String numerocuenta) throws Exception {
        try {
            MCuenta cuenta = iCuenta.findByNumerocuenta(Validaciones.digitos(numerocuenta));
            if (cuenta == null || !Boolean.TRUE.equals(cuenta.getEstado())) {
                throw error(HttpStatus.NOT_FOUND, "No encontramos una cuenta MiPlata con ese número.");
            }
            return toDestino(cuenta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Destino de una transferencia por celular: la primera cuenta (la mas
    // antigua) del usuario que tiene ese celular
    public DestinoResponse buscarDestinoPorCelular(String celular) throws Exception {
        try {
            return toDestino(primeraCuentaPorCelular(celular));
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Public porque SMovimiento la usa para transferir por celular
    public MCuenta primeraCuentaPorCelular(String celular) throws ExcepcionNegocio {
        MPerson persona = iPerson.findByTelephone(Validaciones.digitos(celular));
        List<MCuenta> cuentas = persona == null ? List.of() : iCuenta.findByPersona_IdpersonOrderByIdAsc(persona.getIdperson());
        if (cuentas.isEmpty()) {
            throw error(HttpStatus.NOT_FOUND, "No hay una cuenta MiPlata registrada con ese celular.");
        }
        return cuentas.get(0);
    }

    // Cuenta con la que el usuario hace una operacion: debe existir, ser suya
    // y estar activa. Asi nadie puede mover el dinero de otra persona.
    public MCuenta cuentaDelUsuario(MPerson persona, String numerocuenta) throws ExcepcionNegocio {
        MCuenta cuenta = iCuenta.findByNumerocuenta(Validaciones.digitos(numerocuenta));
        if (cuenta == null) {
            throw error(HttpStatus.NOT_FOUND, "Selecciona una de tus cuentas.");
        }
        if (cuenta.getPersona() == null || cuenta.getPersona().getIdperson() != persona.getIdperson()) {
            throw error(HttpStatus.FORBIDDEN, "La cuenta no pertenece al usuario.");
        }
        if (!Boolean.TRUE.equals(cuenta.getEstado())) {
            throw error(HttpStatus.CONFLICT, "La cuenta está inactiva.");
        }
        return cuenta;
    }

    private static DestinoResponse toDestino(MCuenta cuenta) {
        MPerson titular = cuenta.getPersona();
        return new DestinoResponse(titular.getName(), titular.getUsername(), CuentaMapper.tipo(cuenta), cuenta.getNumerocuenta());
    }

    // =====================  CONSULTAS  =====================

    // Consulta general
    public List<MCuenta> listarCuentas() throws Exception {
        try {
            return iCuenta.findAll();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta por llave primaria
    public MCuenta buscarCuentaPorId(int id) throws Exception {
        try {
            return iCuenta.findById(id)
                    .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una cuenta con id " + id + "."));
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta por numero de cuenta
    public MCuenta buscarCuentaPorNumero(String numerocuenta) throws Exception {
        try {
            MCuenta cuenta = iCuenta.findByNumerocuenta(numerocuenta);
            if (cuenta == null) {
                throw error(HttpStatus.NOT_FOUND, "No existe la cuenta " + numerocuenta + ".");
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

    // Consulta de las cuentas de una persona
    public List<MCuenta> listarCuentasPorPersona(int idperson) throws Exception {
        try {
            return iCuenta.findByPersona_Idperson(idperson);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta de cuentas activas o inactivas
    public List<MCuenta> listarCuentasPorEstado(Boolean estado) throws Exception {
        try {
            return iCuenta.findByEstado(estado);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // =====================  VALIDACIONES  =====================

    // Validacion comun para crear cualquier tipo de cuenta. Es public porque
    // SCuentaAhorro y SCuentaCorriente tambien la usan (reutilizacion).
    public void validarCuentaNueva(MCuenta cuenta) throws Exception {
        try {
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
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    private static ExcepcionNegocio error(HttpStatus estado, String mensaje) {
        return new ExcepcionNegocio(estado, mensaje);
    }
}
