package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MTarjetaCredito;
import com.Cesde.BankingSystem.Repositorio.ICuenta;
import com.Cesde.BankingSystem.Repositorio.ITarjetaCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Servicio de MTarjetaCredito: creacion de tarjetas, consulta de cupo y
// registro de compras y pagos validando la deuda contra el limite de credito.
@Service
public class STarjetaCredito {

    @Autowired
    ITarjetaCredito iTarjetaCredito;

    @Autowired
    ICuenta iCuenta;

    // =====================  ESCRITURA  =====================

    // Adicionar una tarjeta asociada a una cuenta existente
    public MTarjetaCredito adicionarTarjeta(MTarjetaCredito tarjeta, int idCuenta) {
        MCuenta cuenta = iCuenta.findById(idCuenta)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una cuenta con id " + idCuenta + "."));
        if (!Boolean.TRUE.equals(cuenta.getEstado())) {
            throw error(HttpStatus.CONFLICT, "No se puede crear una tarjeta para una cuenta inactiva.");
        }
        String numero = tarjeta.getNumerotarjeta();
        if (numero == null || !numero.matches("\\d{16}")) {
            throw error(HttpStatus.BAD_REQUEST, "El número de tarjeta debe tener 16 dígitos.");
        }
        if (iTarjetaCredito.findByNumerotarjeta(numero) != null) {
            throw error(HttpStatus.CONFLICT, "Ya existe una tarjeta con ese número.");
        }
        if (tarjeta.getCvv() == null || !tarjeta.getCvv().matches("\\d{3}")) {
            throw error(HttpStatus.BAD_REQUEST, "El CVV debe tener 3 dígitos.");
        }
        validarLimite(tarjeta.getLimitecredito());
        if (tarjeta.getFechavencimiento() == null || !tarjeta.getFechavencimiento().isAfter(LocalDate.now())) {
            throw error(HttpStatus.BAD_REQUEST, "La fecha de vencimiento debe ser futura.");
        }
        tarjeta.setCuenta(cuenta);
        tarjeta.setPropietario(cuenta.getPropietario());
        tarjeta.setDeudaactual(0.0);
        tarjeta.setActivo(true);
        tarjeta.setCreatedat(LocalDateTime.now());
        return iTarjetaCredito.save(tarjeta);
    }

    // Modificar: limite de credito y fecha de vencimiento.
    // El numero, el CVV y la deuda no se cambian por aqui.
    public MTarjetaCredito modificarTarjeta(int id, MTarjetaCredito datos) {
        MTarjetaCredito tarjeta = buscarTarjetaPorId(id);
        if (datos.getLimitecredito() != null) {
            validarLimite(datos.getLimitecredito());
            if (datos.getLimitecredito() < tarjeta.getDeudaactual()) {
                throw error(HttpStatus.CONFLICT, "El nuevo límite no puede ser menor a la deuda actual.");
            }
            tarjeta.setLimitecredito(datos.getLimitecredito());
        }
        if (datos.getFechavencimiento() != null) {
            tarjeta.setFechavencimiento(datos.getFechavencimiento());
        }
        return iTarjetaCredito.save(tarjeta);
    }

    // Anular (eliminacion logica): la tarjeta queda inactiva
    public MTarjetaCredito anularTarjeta(int id) {
        MTarjetaCredito tarjeta = buscarTarjetaPorId(id);
        tarjeta.setActivo(false);
        return iTarjetaCredito.save(tarjeta);
    }

    // Eliminar (eliminacion fisica): solo si no tiene deuda pendiente
    public void eliminarTarjeta(int id) {
        MTarjetaCredito tarjeta = buscarTarjetaPorId(id);
        if (tarjeta.getDeudaactual() > 0) {
            throw error(HttpStatus.CONFLICT, "La tarjeta tiene deuda pendiente: no se puede eliminar.");
        }
        iTarjetaCredito.delete(tarjeta);
    }

    // Logica propia: compra con la tarjeta (aumenta la deuda)
    public MTarjetaCredito registrarCompra(int id, Double monto) {
        validarMonto(monto);
        MTarjetaCredito tarjeta = buscarTarjetaPorId(id);
        if (!Boolean.TRUE.equals(tarjeta.getActivo())) {
            throw error(HttpStatus.CONFLICT, "La tarjeta está inactiva.");
        }
        if (tarjeta.getFechavencimiento().isBefore(LocalDate.now())) {
            throw error(HttpStatus.CONFLICT, "La tarjeta está vencida.");
        }
        if (monto > cupoDisponible(tarjeta)) {
            throw error(HttpStatus.CONFLICT, "Cupo insuficiente. Disponible: " + cupoDisponible(tarjeta) + ".");
        }
        tarjeta.setDeudaactual(tarjeta.getDeudaactual() + monto);
        return iTarjetaCredito.save(tarjeta);
    }

    // Logica propia: pago a la tarjeta (disminuye la deuda)
    public MTarjetaCredito registrarPago(int id, Double monto) {
        validarMonto(monto);
        MTarjetaCredito tarjeta = buscarTarjetaPorId(id);
        if (monto > tarjeta.getDeudaactual()) {
            throw error(HttpStatus.BAD_REQUEST, "El pago no puede ser mayor a la deuda (" + tarjeta.getDeudaactual() + ").");
        }
        tarjeta.setDeudaactual(tarjeta.getDeudaactual() - monto);
        return iTarjetaCredito.save(tarjeta);
    }

    // =====================  CONSULTAS  =====================

    // Consulta general
    public List<MTarjetaCredito> listarTarjetas() {
        return iTarjetaCredito.findAll();
    }

    // Consulta por llave primaria
    public MTarjetaCredito buscarTarjetaPorId(int id) {
        return iTarjetaCredito.findById(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una tarjeta con id " + id + "."));
    }

    // Consulta por numero de tarjeta
    public MTarjetaCredito buscarTarjetaPorNumero(String numerotarjeta) {
        MTarjetaCredito tarjeta = iTarjetaCredito.findByNumerotarjeta(numerotarjeta);
        if (tarjeta == null) {
            throw error(HttpStatus.NOT_FOUND, "No existe la tarjeta " + numerotarjeta + ".");
        }
        return tarjeta;
    }

    // Tarjetas de una cuenta
    public List<MTarjetaCredito> listarPorCuenta(int idcuenta) {
        return iTarjetaCredito.findByCuenta_Id(idcuenta);
    }

    // Tarjetas activas o inactivas
    public List<MTarjetaCredito> listarPorEstado(Boolean activo) {
        return iTarjetaCredito.findByActivo(activo);
    }

    // Cupo disponible = limite - deuda
    public double consultarCupoDisponible(int id) {
        return cupoDisponible(buscarTarjetaPorId(id));
    }

    // =====================  VALIDACIONES  =====================

    private double cupoDisponible(MTarjetaCredito tarjeta) {
        return tarjeta.getLimitecredito() - tarjeta.getDeudaactual();
    }

    private void validarLimite(Double limite) {
        if (limite == null || limite <= 0) {
            throw error(HttpStatus.BAD_REQUEST, "El límite de crédito debe ser mayor a 0.");
        }
    }

    private void validarMonto(Double monto) {
        if (monto == null || monto <= 0) {
            throw error(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a 0.");
        }
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}
