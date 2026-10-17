package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Dto.ActualizarContactoRequest;
import com.Cesde.BankingSystem.Dto.CambiarClaveRequest;
import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.LoginRequest;
import com.Cesde.BankingSystem.Dto.RegistroRequest;
import com.Cesde.BankingSystem.Mapper.CuentaMapper;
import com.Cesde.BankingSystem.Mapper.MovimientoMapper;
import com.Cesde.BankingSystem.Mapper.PersonMapper;
import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MPerson;
import com.Cesde.BankingSystem.Repositorio.ICuenta;
import com.Cesde.BankingSystem.Repositorio.IMovimiento;
import com.Cesde.BankingSystem.Repositorio.IPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    // Adicionar una persona: recibe el objeto armado desde afuera (controlador)
    // y delega el guardado al repositorio. Si algo falla (ej. una restriccion
    // de la BD como un email/username repetido), se relanza como Exception
    // para que la capa que llama decida como manejarlo.
    public MPerson adicionarPersona(MPerson mPerson) throws Exception{
        try{
            return this.iPerson.save(mPerson);
        }catch (Exception error){
            throw new Exception(error.getMessage());
        }
    }

    // Registro desde el frontend: crea la persona y su cuenta en una sola
    // transaccion (si falla la cuenta, tampoco queda guardada la persona).
    // Las validaciones son las mismas del formulario de RegisterPage.jsx: el
    // backend las repite porque cualquiera puede llamar la API sin el formulario.
    @Transactional
    public CuentaResponse registrar(RegistroRequest datos) {
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
        persona = iPerson.save(persona);

        MCuenta cuenta = iCuenta.save(CuentaMapper.toEntity(datos, persona, generarNumeroCuenta()));
        return armarRespuesta(persona, cuenta);
    }

    // Inicio de sesion: compara la contrasena con la guardada en la BD y usa
    // failedAttempts/activo para bloquear la cuenta despues de MAX_INTENTOS.
    // No es @Transactional a proposito: si lo fuera, la excepcion de
    // "contrasena incorrecta" desharia el aumento de failedAttempts.
    public CuentaResponse iniciarSesion(LoginRequest datos) {
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
        return armarRespuesta(persona, buscarCuenta(persona));
    }

    // Perfil: el usuario solo puede cambiar su celular y su correo (nombre,
    // documento, usuario y tipo de cuenta no se tocan aunque lleguen en el JSON).
    @Transactional
    public CuentaResponse actualizarContacto(ActualizarContactoRequest datos) {
        MPerson persona = buscarPersona(datos.username());
        String telefono = texto(datos.phone());
        String email = texto(datos.email()).toLowerCase();

        validarTelefono(telefono);
        validarEmail(email);
        if (iPerson.existsByEmailAndIdpersonNot(email, persona.getIdperson())) {
            throw error(HttpStatus.CONFLICT, "Ese correo electrónico ya está registrado por otro usuario.");
        }

        PersonMapper.actualizarContacto(persona, telefono, email);
        persona = iPerson.save(persona);
        return armarRespuesta(persona, buscarCuenta(persona));
    }

    // Perfil: cambio de clave. Pide la clave actual y, si se equivoca, cuenta
    // como intento fallido igual que el login (por eso no es @Transactional).
    public void cambiarClave(CambiarClaveRequest datos) {
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
    }

    // Compara la clave con la guardada y maneja failedAttempts/activo:
    // al llegar a MAX_INTENTOS fallidos la cuenta queda bloqueada.
    private void verificarClave(MPerson persona, String password, String mensajeIncorrecta) {
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

    private MPerson buscarPersona(String username) {
        MPerson persona = iPerson.findByUsername(texto(username).toLowerCase());
        if (persona == null) {
            throw error(HttpStatus.NOT_FOUND, "No encontramos el usuario.");
        }
        return persona;
    }

    private MCuenta buscarCuenta(MPerson persona) {
        MCuenta cuenta = iCuenta.findFirstByPersonaOrderByIdAsc(persona);
        if (cuenta == null) {
            throw error(HttpStatus.NOT_FOUND, "No encontramos una cuenta asociada a este usuario.");
        }
        return cuenta;
    }

    private void validarTelefono(String telefono) {
        if (!telefono.matches("3\\d{9}")) {
            throw error(HttpStatus.BAD_REQUEST, "El celular debe tener 10 dígitos y empezar por 3.");
        }
    }

    private void validarEmail(String email) {
        if (email.length() > 50 || !email.matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}")) {
            throw error(HttpStatus.BAD_REQUEST, "Ingresa un correo electrónico válido, por ejemplo nombre@correo.com.");
        }
    }

    // maximo 50 caracteres: cabe en la columna userpassword (varchar 60)
    private void validarPassword(String password) {
        if (password.length() < 4 || password.length() > 50) {
            throw error(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 4 y 50 caracteres.");
        }
    }

    private void validarRegistro(RegistroRequest datos) {
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

    private CuentaResponse armarRespuesta(MPerson persona, MCuenta cuenta) {
        return CuentaMapper.toResponse(persona, cuenta,
                MovimientoMapper.toResponseList(iMovimiento.findByNumerocuenta(cuenta.getNumerocuenta())));
    }

    // numero de cuenta de 10 digitos que no exista todavia en la BD
    private String generarNumeroCuenta() {
        String numero;
        do {
            numero = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
        } while (iCuenta.existsByNumerocuenta(numero));
        return numero;
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }

    // Pendiente por implementar (misma lista de operaciones que maneja SCliente
    // en el proyecto concesionario, aplicada a Person):
    // Modificar un registro de persona
    // Eliminar un registro de persona
    // consulta general
    // Consulta persona por nombre
    // Consultar las cuentas de una persona
    // Consulta por llave primaria
    // Anular un registro persona

}
