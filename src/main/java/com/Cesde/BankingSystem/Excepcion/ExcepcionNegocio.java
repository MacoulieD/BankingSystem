package com.Cesde.BankingSystem.Excepcion;

import org.springframework.http.HttpStatus;

// Excepcion propia del sistema bancario para los errores de negocio
// (reglas que el usuario incumple: saldo insuficiente, cuenta inexistente,
// correo repetido...).
//
// Extiende Exception, igual que el patron del profe (throws Exception), asi
// que es una excepcion COMPROBADA (checked): el compilador obliga a declararla
// o a atraparla. La diferencia con un "new Exception(mensaje)" es que ademas
// del mensaje guarda el codigo HTTP con el que se debe responder al frontend.
public class ExcepcionNegocio extends Exception {

    private static final long serialVersionUID = 1L;

    private final HttpStatus estado;

    // Error de negocio sin codigo especifico: se responde 400 (Bad Request)
    public ExcepcionNegocio(String mensaje) {
        this(HttpStatus.BAD_REQUEST, mensaje);
    }

    // Error de negocio con su codigo HTTP (404, 409, 401, 403...)
    public ExcepcionNegocio(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
