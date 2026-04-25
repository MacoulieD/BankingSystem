package bankingsystem.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CuentaAhorros {
    private String numeroCuenta;
    private double saldo;
    private final double tasaInteres = 0.015;
    private List<String> movimientos; // Simulación simple de historial

    public CuentaAhorros(String numeroCuenta, double saldoInicial) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldoInicial;
        this.movimientos = new ArrayList<>();
        this.movimientos.add("Apertura de cuenta con: $" + saldoInicial);
    }

    // Getters y Setters
    public String getNumeroCuenta() { return numeroCuenta; }
    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
    public double getTasaInteres() { return tasaInteres; }
    public List<String> getMovimientos() { return movimientos; }

    // Lógica de negocio específica
    public double calcularInteresRendimiento(double monto) {
        return monto * tasaInteres;
    }
}