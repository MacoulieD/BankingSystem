package com.Cesde.BankingSystem.Servicio;

import com.Cesde.BankingSystem.Dto.CompraTarjetaRequest;
import com.Cesde.BankingSystem.Dto.CuentaResponse;
import com.Cesde.BankingSystem.Dto.PagoTarjetaRequest;
import com.Cesde.BankingSystem.Dto.SolicitarTarjetaRequest;
import com.Cesde.BankingSystem.Dto.UsuarioRequest;
import com.Cesde.BankingSystem.Mapper.TarjetaMapper;
import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MMovimientoTarjeta;
import com.Cesde.BankingSystem.Modelo.MPerson;
import com.Cesde.BankingSystem.Modelo.MTarjetaCredito;
import com.Cesde.BankingSystem.Repositorio.ICuenta;
import com.Cesde.BankingSystem.Repositorio.IMovimientoTarjeta;
import com.Cesde.BankingSystem.Repositorio.ITarjetaCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Cesde.BankingSystem.Excepcion.ExcepcionNegocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.Cesde.BankingSystem.Mapper.MapperUtils.texto;

// Servicio de MTarjetaCredito: creacion de tarjetas, consulta de cupo y
// registro de compras y pagos validando la deuda contra el limite de credito.
@Service
public class STarjetaCredito {

    // reglas de la tarjeta (mismos valores de creditCard.js en el frontend)
    public static final double CUPO_MINIMO = 500_000;
    public static final double CUPO_MAXIMO = 20_000_000;
    public static final int MAX_CUOTAS = 36;
    public static final int CUOTAS_SIN_INTERES = 3;
    public static final double TASA_MENSUAL = 0.019;
    private static final int ANIOS_VIGENCIA = 5;

    // tipos de movimiento de la tarjeta (columna movimiento_tarjeta.tipo)
    public static final String COMPRA = "card-purchase";
    public static final String PAGO = "card-payment";

    @Autowired
    ITarjetaCredito iTarjetaCredito;

    @Autowired
    ICuenta iCuenta;

    @Autowired
    IMovimientoTarjeta iMovimientoTarjeta;

    @Autowired
    SPerson sPerson;

    @Autowired
    SCuenta sCuenta;

    @Autowired
    SMovimiento sMovimiento;

    // =====================  OPERACIONES DEL FRONTEND  =====================
    // Mismas reglas de CreditCardPanel.jsx y creditCard.js. Todas reciben el
    // username y devuelven el usuario completo actualizado.

    // Solicitar la tarjeta: una por usuario, ligada a una de sus cuentas
    @Transactional(rollbackFor = Exception.class)
    public CuentaResponse solicitar(SolicitarTarjetaRequest datos) throws Exception {
        try {
            MPerson persona = sPerson.buscarPersonaActiva(datos.username());
            if (iTarjetaCredito.findFirstByCuenta_Persona_Idperson(persona.getIdperson()) != null) {
                throw error(HttpStatus.CONFLICT, "Ya tienes una tarjeta de crédito.");
            }
            MCuenta cuenta = sCuenta.cuentaDelUsuario(persona, datos.accountNumber());
            Double limite = datos.limit();
            if (limite == null || limite != Math.floor(limite) || limite < CUPO_MINIMO || limite > CUPO_MAXIMO) {
                throw error(HttpStatus.BAD_REQUEST, "El cupo debe estar entre " + Validaciones.pesos(CUPO_MINIMO)
                        + " y " + Validaciones.pesos(CUPO_MAXIMO) + ".");
            }
            MTarjetaCredito tarjeta = new MTarjetaCredito();
            tarjeta.setNumerotarjeta(generarNumeroTarjeta());
            tarjeta.setCvv(digitosAleatorios(3));
            tarjeta.setLimitecredito(limite);
            tarjeta.setFechavencimiento(LocalDate.now().plusYears(ANIOS_VIGENCIA));
            // adicionarTarjeta repite las validaciones y llena deuda, estado y fecha
            adicionarTarjeta(tarjeta, cuenta.getId());
            return sPerson.armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Compra a cuotas: la deuda sube por el total CON intereses
    @Transactional(rollbackFor = Exception.class)
    public CuentaResponse comprar(CompraTarjetaRequest datos) throws Exception {
        try {
            MPerson persona = sPerson.buscarPersonaActiva(datos.username());
            MTarjetaCredito tarjeta = tarjetaDelUsuario(persona);
            String comercio = texto(datos.merchant()).replaceAll("\\s+", " ");
            if (comercio.length() < 2 || comercio.length() > 40) {
                throw error(HttpStatus.BAD_REQUEST, "Ingresa el nombre del comercio.");
            }
            double capital = Validaciones.montoEntero(datos.amount());
            Integer cuotas = datos.installments();
            if (cuotas == null || cuotas < 1 || cuotas > MAX_CUOTAS) {
                throw error(HttpStatus.BAD_REQUEST, "El número de cuotas debe estar entre 1 y " + MAX_CUOTAS + ".");
            }
            if (!Boolean.TRUE.equals(tarjeta.getActivo())) {
                throw error(HttpStatus.CONFLICT, "La tarjeta está bloqueada: no permite compras.");
            }
            if (tarjeta.getFechavencimiento().isBefore(LocalDate.now())) {
                throw error(HttpStatus.CONFLICT, "La tarjeta está vencida: no permite compras.");
            }
            double[] plan = planDeCuotas(capital, cuotas);
            double valorCuota = plan[0];
            double total = plan[1];
            if (total > cupoDisponible(tarjeta)) {
                throw error(HttpStatus.CONFLICT, "Cupo insuficiente. Disponible: " + Validaciones.pesos(cupoDisponible(tarjeta))
                        + "; esta compra con intereses suma " + Validaciones.pesos(total) + ".");
            }
            tarjeta.setDeudaactual(tarjeta.getDeudaactual() + total);
            iTarjetaCredito.save(tarjeta);

            MMovimientoTarjeta compra = nuevoMovimiento(tarjeta, COMPRA, "Compra en " + comercio, total);
            compra.setCapital(capital);
            compra.setCuotas(cuotas);
            compra.setValorcuota(valorCuota);
            compra.setSaldopendiente(total);
            iMovimientoTarjeta.save(compra);
            return sPerson.armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Pago desde una cuenta del usuario: descuenta de la cuenta, resta la deuda
    // y abona a las compras pendientes de la mas antigua a la mas reciente.
    // Se permite aunque la tarjeta este bloqueada o vencida.
    @Transactional(rollbackFor = Exception.class)
    public CuentaResponse pagar(PagoTarjetaRequest datos) throws Exception {
        try {
            MPerson persona = sPerson.buscarPersonaActiva(datos.username());
            MTarjetaCredito tarjeta = tarjetaDelUsuario(persona);
            MCuenta cuenta = sCuenta.cuentaDelUsuario(persona, datos.fromAccountNumber());
            double monto = Validaciones.montoEntero(datos.amount());
            if (tarjeta.getDeudaactual() <= 0) {
                throw error(HttpStatus.BAD_REQUEST, "No tienes deuda pendiente.");
            }
            if (monto > tarjeta.getDeudaactual()) {
                throw error(HttpStatus.BAD_REQUEST, "El pago no puede ser mayor a la deuda (" + Validaciones.pesos(tarjeta.getDeudaactual()) + ").");
            }
            // en la cuenta corriente tambien cuenta el sobregiro
            if (monto > SMovimiento.disponible(cuenta)) {
                throw error(HttpStatus.CONFLICT, "No tienes saldo suficiente en esa cuenta.");
            }

            sMovimiento.registrarMovimiento(cuenta, SMovimiento.PAGO_TARJETA, -monto,
                    "Pago tarjeta de crédito ****" + TarjetaMapper.ultimos4(tarjeta.getNumerotarjeta()));

            tarjeta.setDeudaactual(tarjeta.getDeudaactual() - monto);
            iTarjetaCredito.save(tarjeta);

            double restante = monto;
            for (MMovimientoTarjeta movimiento : iMovimientoTarjeta.findByTarjeta_IdOrderByFechaAscIdAsc(tarjeta.getId())) {
                if (restante <= 0) {
                    break;
                }
                if (COMPRA.equals(movimiento.getTipo()) && movimiento.getSaldopendiente() != null && movimiento.getSaldopendiente() > 0) {
                    double abono = Math.min(movimiento.getSaldopendiente(), restante);
                    movimiento.setSaldopendiente(movimiento.getSaldopendiente() - abono);
                    restante -= abono;
                    iMovimientoTarjeta.save(movimiento);
                }
            }

            iMovimientoTarjeta.save(nuevoMovimiento(tarjeta, PAGO, "Pago desde cuenta ****"
                    + cuenta.getNumerocuenta().substring(cuenta.getNumerocuenta().length() - 4), monto));
            return sPerson.armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Bloquear: ya no permite compras y no se puede desbloquear (se puede seguir pagando)
    public CuentaResponse bloquear(UsuarioRequest datos) throws Exception {
        try {
            MPerson persona = sPerson.buscarPersonaActiva(datos.username());
            MTarjetaCredito tarjeta = tarjetaDelUsuario(persona);
            if (!Boolean.TRUE.equals(tarjeta.getActivo())) {
                throw error(HttpStatus.CONFLICT, "La tarjeta ya está bloqueada.");
            }
            tarjeta.setActivo(false);
            iTarjetaCredito.save(tarjeta);
            return sPerson.armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Cancelar: solo sin deuda. Se borran la tarjeta y su historial (el
    // frontend dice "se elimina definitivamente"); luego puede pedir otra.
    @Transactional(rollbackFor = Exception.class)
    public CuentaResponse cancelar(UsuarioRequest datos) throws Exception {
        try {
            MPerson persona = sPerson.buscarPersonaActiva(datos.username());
            MTarjetaCredito tarjeta = tarjetaDelUsuario(persona);
            if (tarjeta.getDeudaactual() > 0) {
                throw error(HttpStatus.CONFLICT, "La tarjeta tiene deuda pendiente: no se puede cancelar.");
            }
            iMovimientoTarjeta.deleteByTarjeta_Id(tarjeta.getId());
            iTarjetaCredito.delete(tarjeta);
            return sPerson.armarRespuesta(persona);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Plan de cuotas, igual que installmentPlan de creditCard.js:
    //  - hasta 3 cuotas sin interes: cuota = techo(capital / cuotas), total = capital
    //  - desde 4 cuotas, interes mensual con cuota fija (amortizacion francesa):
    //    cuota = techo(capital * i / (1 - (1 + i)^-n)), total = cuota * n
    // Devuelve {cuota, total}
    public static double[] planDeCuotas(double capital, int cuotas) {
        if (cuotas <= CUOTAS_SIN_INTERES) {
            return new double[]{Math.ceil(capital / cuotas), capital};
        }
        double cuota = Math.ceil((capital * TASA_MENSUAL) / (1 - Math.pow(1 + TASA_MENSUAL, -cuotas)));
        return new double[]{cuota, cuota * cuotas};
    }

    private MTarjetaCredito tarjetaDelUsuario(MPerson persona) throws ExcepcionNegocio {
        MTarjetaCredito tarjeta = iTarjetaCredito.findFirstByCuenta_Persona_Idperson(persona.getIdperson());
        if (tarjeta == null) {
            throw error(HttpStatus.NOT_FOUND, "No tienes una tarjeta de crédito.");
        }
        return tarjeta;
    }

    private MMovimientoTarjeta nuevoMovimiento(MTarjetaCredito tarjeta, String tipo, String descripcion, double monto) {
        MMovimientoTarjeta movimiento = new MMovimientoTarjeta();
        movimiento.setTarjeta(tarjeta);
        movimiento.setTipo(tipo);
        movimiento.setDescripcion(descripcion);
        movimiento.setMonto(monto);
        movimiento.setDeudaposterior(tarjeta.getDeudaactual());
        movimiento.setFecha(LocalDateTime.now());
        return movimiento;
    }

    // 16 digitos que empiezan por 4 y no existen todavia en la BD
    private String generarNumeroTarjeta() {
        String numero;
        do {
            numero = "4" + digitosAleatorios(15);
        } while (iTarjetaCredito.existsByNumerotarjeta(numero));
        return numero;
    }

    private static String digitosAleatorios(int cantidad) {
        StringBuilder digitos = new StringBuilder();
        for (int i = 0; i < cantidad; i++) {
            digitos.append(ThreadLocalRandom.current().nextInt(10));
        }
        return digitos.toString();
    }

    // =====================  ESCRITURA  =====================

    // Adicionar una tarjeta asociada a una cuenta existente
    public MTarjetaCredito adicionarTarjeta(MTarjetaCredito tarjeta, int idCuenta) throws Exception {
        try {
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
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Modificar: limite de credito y fecha de vencimiento.
    // El numero, el CVV y la deuda no se cambian por aqui.
    public MTarjetaCredito modificarTarjeta(int id, MTarjetaCredito datos) throws Exception {
        try {
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
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Anular (eliminacion logica): la tarjeta queda inactiva
    public MTarjetaCredito anularTarjeta(int id) throws Exception {
        try {
            MTarjetaCredito tarjeta = buscarTarjetaPorId(id);
            tarjeta.setActivo(false);
            return iTarjetaCredito.save(tarjeta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Eliminar (eliminacion fisica): solo si no tiene deuda pendiente
    public void eliminarTarjeta(int id) throws Exception {
        try {
            MTarjetaCredito tarjeta = buscarTarjetaPorId(id);
            if (tarjeta.getDeudaactual() > 0) {
                throw error(HttpStatus.CONFLICT, "La tarjeta tiene deuda pendiente: no se puede eliminar.");
            }
            iTarjetaCredito.delete(tarjeta);
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Logica propia: compra con la tarjeta (aumenta la deuda)
    public MTarjetaCredito registrarCompra(int id, Double monto) throws Exception {
        try {
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
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Logica propia: pago a la tarjeta (disminuye la deuda)
    public MTarjetaCredito registrarPago(int id, Double monto) throws Exception {
        try {
            validarMonto(monto);
            MTarjetaCredito tarjeta = buscarTarjetaPorId(id);
            if (monto > tarjeta.getDeudaactual()) {
                throw error(HttpStatus.BAD_REQUEST, "El pago no puede ser mayor a la deuda (" + tarjeta.getDeudaactual() + ").");
            }
            tarjeta.setDeudaactual(tarjeta.getDeudaactual() - monto);
            return iTarjetaCredito.save(tarjeta);
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
    public List<MTarjetaCredito> listarTarjetas() throws Exception {
        try {
            return iTarjetaCredito.findAll();
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta por llave primaria
    public MTarjetaCredito buscarTarjetaPorId(int id) throws Exception {
        try {
            return iTarjetaCredito.findById(id)
                    .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe una tarjeta con id " + id + "."));
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Consulta por numero de tarjeta
    public MTarjetaCredito buscarTarjetaPorNumero(String numerotarjeta) throws Exception {
        try {
            MTarjetaCredito tarjeta = iTarjetaCredito.findByNumerotarjeta(numerotarjeta);
            if (tarjeta == null) {
                throw error(HttpStatus.NOT_FOUND, "No existe la tarjeta " + numerotarjeta + ".");
            }
            return tarjeta;
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Tarjetas de una cuenta
    public List<MTarjetaCredito> listarPorCuenta(int idcuenta) throws Exception {
        try {
            return iTarjetaCredito.findByCuenta_Id(idcuenta);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Tarjetas activas o inactivas
    public List<MTarjetaCredito> listarPorEstado(Boolean activo) throws Exception {
        try {
            return iTarjetaCredito.findByActivo(activo);
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // Cupo disponible = limite - deuda
    public double consultarCupoDisponible(int id) throws Exception {
        try {
            return cupoDisponible(buscarTarjetaPorId(id));
        } catch (ExcepcionNegocio error) {
            // error de negocio: se relanza tal cual (conserva su mensaje y codigo HTTP)
            throw error;
        } catch (Exception error) {
            // error inesperado: se relanza con su mensaje y la causa original
            throw new Exception(error.getMessage(), error);
        }
    }

    // =====================  VALIDACIONES  =====================

    private double cupoDisponible(MTarjetaCredito tarjeta) {
        return tarjeta.getLimitecredito() - tarjeta.getDeudaactual();
    }

    private void validarLimite(Double limite) throws ExcepcionNegocio {
        if (limite == null || limite <= 0) {
            throw error(HttpStatus.BAD_REQUEST, "El límite de crédito debe ser mayor a 0.");
        }
    }

    private void validarMonto(Double monto) throws ExcepcionNegocio {
        if (monto == null || monto <= 0) {
            throw error(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a 0.");
        }
    }

    private static ExcepcionNegocio error(HttpStatus estado, String mensaje) {
        return new ExcepcionNegocio(estado, mensaje);
    }
}
