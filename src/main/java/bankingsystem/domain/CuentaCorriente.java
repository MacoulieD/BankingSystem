package bankingsystem.domain;

import java.util.List;

public class CuentaCorriente extends Cuenta {
    private double cupoSobregiro;

    public CuentaCorriente(String numeroCuenta, double saldo, String propietario, double cupoSobregiro) {
        super(numeroCuenta, saldo, propietario);
        this.cupoSobregiro = cupoSobregiro;
    }

    // --- Getters y Setters ---

    @Override
    public String getNumeroCuenta() {
        return this.numeroCuenta;
    }

    @Override
    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    @Override
    public double getSaldo() {
        return this.saldo;
    }

    @Override
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public double getCupoSobregiro() {
        return cupoSobregiro;
    }

    public void setCupoSobregiro(double cupoSobregiro) {
        this.cupoSobregiro = cupoSobregiro;
    }

    @Override
    public List<String> getMovimientos() {
        return this.movimientos;
    }

    @Override
    public void setMovimientos(List<String> movimientos) {
        this.movimientos = movimientos;
    }

    public String getTypoCuenta() {
        return "Corriente";
    }
}