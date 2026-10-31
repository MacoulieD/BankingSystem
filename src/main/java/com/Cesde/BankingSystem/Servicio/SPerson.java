package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Dto.ActualizarContactoRequest;
import com.Cesde.BankingSystem.Dto.CambiarClaveRequest;
import com.Cesde.BankingSystem.Dto.CuentaBancariaResponse;
import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.LoginRequest;
import com.Cesde.BankingSystem.Dto.RegistroRequest;
import com.Cesde.BankingSystem.Dto.TarjetaResponse;
import com.Cesde.BankingSystem.Mapper.CuentaMapper;
import com.Cesde.BankingSystem.Mapper.MovimientoMapper;
import com.Cesde.BankingSystem.Mapper.PersonMapper;
import com.Cesde.BankingSystem.Mapper.TarjetaMapper;
import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MPerson;
import com.Cesde.BankingSystem.Modelo.MTarjetaCredito;
import com.Cesde.BankingSystem.Repositorio.ICuenta;
import com.Cesde.BankingSystem.Repositorio.IMovimiento;
import com.Cesde.BankingSystem.Repositorio.IMovimientoTarjeta;
import com.Cesde.BankingSystem.Repositorio.IPerson;
import com.Cesde.BankingSystem.Repositorio.ITarjetaCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Cesde.BankingSystem.Excepcion.ExcepcionNegocio;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.Cesde.BankingSystem.Mapper.MapperUtils.texto;

// Capa de servicio de MPerson: aqui va la logica de negocio, separada del
// repositorio (acceso a datos), del controlador (CAutenticacion) y de los
// mappers (conversion entre entidades y DTOs, paquete Mapper).
@Service
public class SPerson {
    // intentos fallidos permitidos antes de bloquear la cuenta
    private static final int MAX_INTENTOS = 3;

    // Spring inyecta automaticamente la implementacion de IPerson
    @Autowired
    IPerson iPerson;
    @Autowired
    ICuenta iCuenta;
    @Autowired
    IMovimiento iMovimiento;
    @Autowired
    ITarjetaCredito iTarjetaCredito;
    @Autowired
    IMovimientoTarjeta iMovimientoTarjeta;

    // Adicionar una persona: recibe el objeto armado desde afuera (controlador)
    // y delega el guardado al repositorio. Si algo falla (ej. una restriccion
    // de la BD como un email/username repetido), se relanza como Exception
    // para que la capa que llama decida como manejarlo.
    public MPerson adicionarPersona(MPerson mPerson) throws Exception{
        try{
            return this.iPerson.save(mPerson);
        }catch (Exception error){
            throw new Exception(error.getMessage(), error);
        }
    }

    // Registro desde el frontend: crea la persona y su cuenta en una sola
    // transaccion (si falla la cuenta, tampoco queda guardada la persona).
    // Las validaciones son las mismas del formulario de RegisterPage.jsx: el
    // backend las repite porque cualquiera puede llamar la API sin el formulario.
    @Transactional(rollbackFor = Exception.class)
    public CuentaResponse registrar(RegistroRequest datos) throws Exception {
        try {
            validarRegistro(datos);

            MPerson persona = PersonMapper.toEntity(datos);
            if (iPerson.existsByUsername(persona.getUsername())) {
                throw error(HttpStatus.CONFLICT, "Ese nombre de usuario ya está registrado.");
            }
            if (iPerson.existsByDocumento(persona.getDocumento())) {
                throw error(HttpStatus.CONFLICT, "Ya existe una cuenta con ese número de documento.");
            }
            if (iPerson.existsByEmail(persona.getEmail())) {
                throw error(HttpStatus.CONFLICT, "Ese correo electrónico ya está registrado.");
            }
            if (iPerson.existsByTelephone(persona.getTelephone())) {
                throw error(HttpStatus.CONFLICT, "Ese número de celular ya está registrado.");
            }
            persona = iPerson.save(persona);

            iCuenta.save(CuentaMapper.toEntity(datos, persona, generarNumeroCuenta()));
            return armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Inicio de sesion: compara la contrasena con la guardada en la BD y usa
    // failedAttempts/activo para bloquear la cuenta despues de MAX_INTENTOS.
    // No es @Transactional a proposito: si lo fuera, la excepcion de
    // "contrasena incorrecta" desharia el aumento de failedAttempts.
    public CuentaResponse iniciarSesion(LoginRequest datos) throws Exception {
        try {
            String username = texto(datos.username()).toLowerCase();
            String password = datos.password() == null ? "" : datos.password();

            if (username.isEmpty() || password.isEmpty()) {
                throw error(HttpStatus.BAD_REQUEST, "Ingresa tu usuario y contraseña.");
            }

            MPerson persona = iPerson.findByUsername(username);
            if (persona == null) {
                throw error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos. Verifica los datos o crea una cuenta.");
            }
            verificarClave(persona, password, "Usuario o contraseña incorrectos. Verifica los datos o crea una cuenta.");
            // el dashboard necesita al menos una cuenta
            buscarCuenta(persona);
            return armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Perfil: el usuario solo puede cambiar su celular y su correo (nombre,
    // documento, usuario y tipo de cuenta no se tocan aunque lleguen en el JSON).
    @Transactional(rollbackFor = Exception.class)
    public CuentaResponse actualizarContacto(ActualizarContactoRequest datos) throws Exception {
        try {
            MPerson persona = buscarPersona(datos.username());
            String telefono = texto(datos.phone());
            String email = texto(datos.email()).toLowerCase();

            validarTelefono(telefono);
            validarEmail(email);
            if (iPerson.existsByEmailAndIdpersonNot(email, persona.getIdperson())) {
                throw error(HttpStatus.CONFLICT, "Ese correo electrónico ya está registrado por otro usuario.");
            }
            if (iPerson.existsByTelephoneAndIdpersonNot(telefono, persona.getIdperson())) {
                throw error(HttpStatus.CONFLICT, "Ese número de celular ya está registrado por otro usuario.");
            }

            PersonMapper.actualizarContacto(persona, telefono, email);
            persona = iPerson.save(persona);
            return armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Perfil: cambio de clave. Pide la clave actual y, si se equivoca, cuenta
    // como intento fallido igual que el login (por eso no es @Transactional).
    public void cambiarClave(CambiarClaveRequest datos) throws Exception {
        try {
            MPerson persona = buscarPersona(datos.username());
            String claveActual = datos.currentPassword() == null ? "" : datos.currentPassword();
            String claveNueva = datos.newPassword() == null ? "" : datos.newPassword();

            if (claveActual.isEmpty()) {
                throw error(HttpStatus.BAD_REQUEST, "Ingresa tu clave actual.");
            }
            validarPassword(claveNueva);
            verificarClave(persona, claveActual, "La clave actual es incorrecta.");
            if (claveNueva.equals(claveActual)) {
                throw error(HttpStatus.BAD_REQUEST, "La nueva clave debe ser diferente a la actual.");
            }

            persona.setUserpassword(claveNueva);
            iPerson.save(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Compara la clave con la guardada y maneja failedAttempts/activo:
    // al llegar a MAX_INTENTOS fallidos la cuenta queda bloqueada.
    private void verificarClave(MPerson persona, String password, String mensajeIncorrecta) throws ExcepcionNegocio {
        if (!Boolean.TRUE.equals(persona.getActivo())) {
            throw error(HttpStatus.FORBIDDEN, "Tu cuenta está bloqueada por intentos fallidos. Comunícate con soporte.");
        }
        if (!password.equals(persona.getUserpassword())) {
            int intentos = persona.getFailedAttempts() + 1;
            persona.setFailedAttempts(intentos);
            if (intentos >= MAX_INTENTOS) {
                persona.setActivo(false);
            }
            iPerson.save(persona);
            if (intentos >= MAX_INTENTOS) {
                throw error(HttpStatus.FORBIDDEN, "Tu cuenta fue bloqueada por " + MAX_INTENTOS + " intentos fallidos. Comunícate con soporte.");
            }
            throw error(HttpStatus.UNAUTHORIZED, mensajeIncorrecta);
        }
        if (persona.getFailedAttempts() != 0) {
            persona.setFailedAttempts(0);
            iPerson.save(persona);
        }
    }

    // Public porque las operaciones (SCuenta, SMovimiento, STarjetaCredito)
    // identifican al usuario por el username que envia el frontend
    public MPerson buscarPersona(String username) throws ExcepcionNegocio {
        MPerson persona = iPerson.findByUsername(texto(username).toLowerCase());
        if (persona == null) {
            throw error(HttpStatus.NOT_FOUND, "No encontramos el usuario.");
        }
        return persona;
    }

    // Para las operaciones de dinero: el usuario debe existir y no estar bloqueado
    public MPerson buscarPersonaActiva(String username) throws ExcepcionNegocio {
        MPerson persona = buscarPersona(username);
        if (!Boolean.TRUE.equals(persona.getActivo())) {
            throw error(HttpStatus.FORBIDDEN, "Tu cuenta está bloqueada por intentos fallidos. Comunícate con soporte.");
        }
        return persona;
    }

    private MCuenta buscarCuenta(MPerson persona) throws ExcepcionNegocio {
        MCuenta cuenta = iCuenta.findFirstByPersonaOrderByIdAsc(persona);
        if (cuenta == null) {
            throw error(HttpStatus.NOT_FOUND, "No encontramos una cuenta asociada a este usuario.");
        }
        return cuenta;
    }

    private void validarTelefono(String telefono) throws ExcepcionNegocio {
        if (!telefono.matches("3\\d{9}")) {
            throw error(HttpStatus.BAD_REQUEST, "El celular debe tener 10 dígitos y empezar por 3.");
        }
    }

    private void validarEmail(String email) throws ExcepcionNegocio {
        if (email.length() > 50 || !email.matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}")) {
            throw error(HttpStatus.BAD_REQUEST, "Ingresa un correo electrónico válido, por ejemplo nombre@correo.com.");
        }
    }

    // maximo 50 caracteres: cabe en la columna userpassword (varchar 60)
    private void validarPassword(String password) throws ExcepcionNegocio {
        if (password.length() < 4 || password.length() > 50) {
            throw error(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 4 y 50 caracteres.");
        }
    }

    private void validarRegistro(RegistroRequest datos) throws ExcepcionNegocio {
        String tipoDocumento = texto(datos.documentType());
        String documento = texto(datos.documentNumber()).toUpperCase();
        String telefono = texto(datos.phone());
        String email = texto(datos.email()).toLowerCase();
        String nombre = texto(datos.fullName()).replaceAll("\\s+", " ");
        String username = texto(datos.username()).toLowerCase();
        String password = datos.password() == null ? "" : datos.password();
        String tipoCuenta = texto(datos.accountType());
        Long saldo = datos.balance();

        if (!tipoDocumento.equals("CC") && !tipoDocumento.equals("PA")) {
            throw error(HttpStatus.BAD_REQUEST, "Tipo de documento no válido.");
        }
        if (tipoDocumento.equals("CC") && !documento.matches("\\d{6,10}")) {
            throw error(HttpStatus.BAD_REQUEST, "La cédula debe tener solo números, entre 6 y 10 dígitos.");
        }
        if (tipoDocumento.equals("PA") && !documento.matches("[A-Z0-9]{6,9}")) {
            throw error(HttpStatus.BAD_REQUEST, "El pasaporte debe tener entre 6 y 9 caracteres, solo letras y números.");
        }
        validarTelefono(telefono);
        validarEmail(email);
        if (nombre.length() > 60 || !nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]+(\\s[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]+)+")) {
            throw error(HttpStatus.BAD_REQUEST, "Ingresa tu nombre y apellido, solo con letras.");
        }
        if (!username.matches("[a-z0-9_]{4,20}")) {
            throw error(HttpStatus.BAD_REQUEST, "El usuario debe tener entre 4 y 20 caracteres: letras, números o guion bajo.");
        }
        validarPassword(password);
        if (!tipoCuenta.equals(CuentaMapper.TIPO_AHORRO) && !tipoCuenta.equals(CuentaMapper.TIPO_CORRIENTE)) {
            throw error(HttpStatus.BAD_REQUEST, "Tipo de cuenta no válido.");
        }
        if (saldo == null || saldo < 0) {
            throw error(HttpStatus.BAD_REQUEST, "El saldo inicial debe ser un número entero mayor o igual a 0.");
        }
    }

    // Respuesta de todas las operaciones: la persona con todas sus cuentas
    // (de la mas antigua a la mas reciente), los movimientos de cada una y su
    // tarjeta de credito (o null). Public porque la usan los demas servicios.
    public CuentaResponse armarRespuesta(MPerson persona) {
        List<CuentaBancariaResponse> cuentas = iCuenta.findByPersona_IdpersonOrderByIdAsc(persona.getIdperson()).stream()
                .map(cuenta -> CuentaMapper.toCuentaBancaria(cuenta,
                        MovimientoMapper.toResponseList(iMovimiento.findByCuenta_IdOrderByFechaAscIdAsc(cuenta.getId()))))
                .toList();
        MTarjetaCredito tarjeta = iTarjetaCredito.findFirstByCuenta_Persona_Idperson(persona.getIdperson());
        TarjetaResponse respuestaTarjeta = tarjeta == null ? null
                : TarjetaMapper.toResponse(tarjeta, iMovimientoTarjeta.findByTarjeta_IdOrderByFechaAscIdAsc(tarjeta.getId()));
        return CuentaMapper.toResponse(persona, cuentas, respuestaTarjeta);
    }

    // numero de cuenta de 10 digitos que no exista todavia en la BD.
    // Public porque SCuenta.abrirCuenta tambien lo usa.
    public String generarNumeroCuenta() {
        String numero;
        do {
            numero = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
        } while (iCuenta.existsByNumerocuenta(numero));
        return numero;
    }

    private static ExcepcionNegocio error(HttpStatus estado, String mensaje) {
        return new ExcepcionNegocio(estado, mensaje);
    }

    // =====================  CRUD Y CONSULTAS DE PERSONA  =====================
    // (lo que estaba pendiente: misma lista de operaciones de SCliente en el
    // proyecto concesionario, aplicada a Person)

    // Consulta general
    public List<MPerson> listarPersonas() throws Exception {
        try {
            return iPerson.findAll();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta por llave primaria
    public MPerson buscarPersonaPorId(int id) throws Exception {
        try {
            return iPerson.findById(id)
                    .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una persona con id " + id + "."));
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta persona por nombre (puede haber varias con el mismo nombre)
    public List<MPerson> buscarPersonasPorNombre(String nombre) throws Exception {
        try {
            return iPerson.findByName(texto(nombre).replaceAll("\\s+", " "));
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consultar las cuentas de una persona
    public List<MCuenta> listarCuentasDePersona(int id) throws Exception {
        try {
            buscarPersonaPorId(id);
            return iCuenta.findByPersona_Idperson(id);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Modificar un registro de persona: nombre, telefono y correo.
    // Documento, usuario y clave no se cambian aqui (la clave tiene su propio
    // metodo, cambiarClave, que exige la clave actual).
    public MPerson modificarPersona(int id, MPerson datos) throws Exception {
        try {
            MPerson persona = buscarPersonaPorId(id);
            if (datos.getName() != null && !datos.getName().isBlank()) {
                persona.setName(texto(datos.getName()).replaceAll("\\s+", " "));
            }
            if (datos.getTelephone() != null) {
                String telefono = texto(datos.getTelephone());
                validarTelefono(telefono);
                if (iPerson.existsByTelephoneAndIdpersonNot(telefono, id)) {
                    throw error(HttpStatus.CONFLICT, "Ese número de celular ya está registrado por otro usuario.");
                }
                persona.setTelephone(telefono);
            }
            if (datos.getEmail() != null) {
                String email = texto(datos.getEmail()).toLowerCase();
                validarEmail(email);
                if (iPerson.existsByEmailAndIdpersonNot(email, id)) {
                    throw error(HttpStatus.CONFLICT, "Ese correo electrónico ya está registrado por otro usuario.");
                }
                persona.setEmail(email);
            }
            return iPerson.save(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Anular un registro de persona (eliminacion logica): queda inactiva y no
    // puede iniciar sesion, pero se conservan sus datos e historial.
    public MPerson anularPersona(int id) throws Exception {
        try {
            MPerson persona = buscarPersonaPorId(id);
            persona.setActivo(false);
            return iPerson.save(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Reactivar una persona bloqueada (por intentos fallidos o anulada)
    public MPerson reactivarPersona(int id) throws Exception {
        try {
            MPerson persona = buscarPersonaPorId(id);
            persona.setActivo(true);
            persona.setFailedAttempts(0);
            return iPerson.save(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Eliminar un registro de persona (eliminacion fisica): solo si no tiene
    // cuentas, porque cuenta.pkpersona es llave foranea hacia person.
    public void eliminarPersona(int id) throws Exception {
        try {
            MPerson persona = buscarPersonaPorId(id);
            if (!iCuenta.findByPersona_Idperson(id).isEmpty()) {
                throw error(HttpStatus.CONFLICT, "La persona tiene cuentas asociadas: no se puede eliminar, solo anular.");
            }
            iPerson.delete(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }
}
