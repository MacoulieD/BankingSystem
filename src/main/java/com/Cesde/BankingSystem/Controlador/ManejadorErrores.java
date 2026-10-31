package com.Cesde.BankingSystem.Controlador;

import com.Cesde.BankingSystem.Dto.RespuestaError;
import com.Cesde.BankingSystem.Excepcion.ExcepcionNegocio;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

// Manejador GLOBAL de errores de la API.
// @RestControllerAdvice hace que esta clase "escuche" las excepciones que
// salen de CUALQUIER controlador. Cada @ExceptionHandler atiende un tipo de
// excepcion y la convierte en una respuesta HTTP con el formato RespuestaError.
// Asi los controladores no necesitan try/catch: solo declaran throws Exception.
@RestControllerAdvice
public class ManejadorErrores {

    // registro (log) de errores en la consola del servidor
    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    // 1. Errores de negocio lanzados por los servicios (ExcepcionNegocio):
    //    se responde con el codigo y el mensaje que trae la excepcion.
    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<RespuestaError> errorDeNegocio(ExcepcionNegocio error, HttpServletRequest peticion) {
        return responder(error.getEstado(), error.getMessage(), peticion);
    }

    // 2. JSON mal formado (ej. un texto donde se esperaba el numero del saldo)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> datosInvalidos(HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos.", peticion);
    }

    // 3. Parametro de la URL con tipo incorrecto (ej. /api/cuentas/abc cuando se espera un numero)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> parametroInvalido(MethodArgumentTypeMismatchException error, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + error.getName() + "' no tiene un valor válido.", peticion);
    }

    // 4. Restriccion UNIQUE o de llave foranea de la BD (ej. dos registros
    //    iguales al mismo tiempo)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> datoRepetido(HttpServletRequest peticion) {
        return responder(HttpStatus.CONFLICT, MENSAJE_INTEGRIDAD, peticion);
    }

    // 5. Cualquier otra excepcion (la "red de seguridad").
    //    Los servicios envuelven los errores inesperados en una Exception con
    //    la causa original; aqui se revisa esa causa para responder bien.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> errorGeneral(Exception error, HttpServletRequest peticion) {
        // excepciones propias de Spring que ya traen su codigo (ruta que no
        // existe = 404, metodo HTTP no permitido = 405...)
        if (error instanceof ErrorResponse respuestaSpring) {
            return responder(respuestaSpring.getStatusCode(), error.getMessage(), peticion);
        }
        // error de la BD que llego envuelto desde un servicio
        if (contieneCausa(error, DataIntegrityViolationException.class)) {
            return responder(HttpStatus.CONFLICT, MENSAJE_INTEGRIDAD, peticion);
        }
        // error inesperado: el detalle tecnico se registra en la consola del
        // servidor, pero al cliente NO se le muestra (podria revelar SQL,
        // nombres de tablas, etc.)
        log.error("Error inesperado en {}", peticion.getRequestURI(), error);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta de nuevo más tarde.", peticion);
    }

    // ---------------------------------------------------------------------

    private static final String MENSAJE_INTEGRIDAD =
            "El dato ya está registrado o tiene información relacionada.";

    // arma la respuesta con el formato RespuestaError
    private ResponseEntity<RespuestaError> responder(HttpStatusCode estado, String mensaje, HttpServletRequest peticion) {
        HttpStatus status = HttpStatus.resolve(estado.value());
        RespuestaError cuerpo = new RespuestaError(
                LocalDateTime.now(),
                estado.value(),
                status != null ? status.getReasonPhrase() : "Error",
                mensaje,
                peticion.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }

    // recorre la cadena de causas (getCause) buscando un tipo de excepcion
    private static boolean contieneCausa(Throwable error, Class<? extends Throwable> tipo) {
        for (Throwable actual = error; actual != null; actual = actual.getCause()) {
            if (tipo.isInstance(actual)) {
                return true;
            }
        }
        return false;
    }
}
