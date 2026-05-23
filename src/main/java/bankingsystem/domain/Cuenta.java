package bankingsystem.domain;

import bankingsystem.domain.enums.EstadoCuenta;
import bankingsystem.domain.enums.TypoCuenta;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Cuenta {
    protected String numeroCuenta;
    protected double saldo;
    protected String propietario;
    protected TypoCuenta tipo;
    protected LocalDateTime fechaApertura;
    protected EstadoCuenta estado;
    protected List<Movimiento> movimientos;

    public Cuenta(String numeroCuenta, double saldo, String propietario) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.propietario = propietario;
        this.tipo = null;
        this.fechaApertura = LocalDateTime.now();
        this.estado = EstadoCuenta.ACTIVA;
        this.movimientos = new ArrayList<>();
    }

    // Getters y Setters
    public TypoCuenta getTipo() { return tipo; }
    public void setTipo(TypoCuenta tipo) { this.tipo = tipo; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public String getPropietario() { return propietario; }

    public void setPropietario(String propietario) { this.propietario = propietario; }

    public List<Movimiento> getMovimientos() { return movimientos; }

    public abstract void setMovimientos(List<Movimiento> movimientos);

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public EstadoCuenta getEstado() { return estado; }
    public void setEstado(EstadoCuenta estado) { this.estado = estado; }
}