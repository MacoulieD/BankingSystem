package bankingsystem.domain;

import bankingsystem.domain.enums.TypoCuenta;
import java.util.List;

public class CuentaAhorros extends Cuenta {

    private final double tasaInteres = 0.015;

    public CuentaAhorros(String numeroCuenta, double saldo, String propietario) {

        super(numeroCuenta, saldo, propietario);
        this.tipo = TypoCuenta.AHORROS;
    }

    // --- Getters y Setters ---

    @Override
    public String getNumeroCuenta() {
        return this.numeroCuenta;
    }

    public String getTypoCuenta() {
        return "Ahorros";
    }

    @Override
    public double getSaldo() {
        return this.saldo;
    }

    @Override
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public double getTasaInteres() {
        return tasaInteres;
    }

    @Override
    public List<String> getMovimientos() {
        return this.movimientos;
    }

    @Override
    public void setMovimientos(List<String> movimientos) {}


    public double calcularInteresRendimiento(double monto) {
        return monto * tasaInteres;
    }
}