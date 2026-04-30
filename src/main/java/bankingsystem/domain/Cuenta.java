package bankingsystem.domain;

import bankingsystem.domain.enums.TypoCuenta;
import java.util.ArrayList;
import java.util.List;

public abstract class Cuenta {
    protected String numeroCuenta;
    protected double saldo;
    protected String propietario;
    protected TypoCuenta tipo; // <-- Nuevo atributo
    protected List<String> movimientos;

    public Cuenta(String numeroCuenta, double saldo, String propietario) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.propietario = propietario;
        this.tipo = tipo; //Correjir no toma el tipó de cuenta(1/2)
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

    public List<String> getMovimientos() { return movimientos; }

    public abstract void setMovimientos(List<String> movimientos);
}