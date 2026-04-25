package bankingsystem.domain;

import java.util.ArrayList;
import java.util.List;

public class CuentaCorriente {
    private String numeroCuenta;
    private double saldo;
    private final double porcentajeSobregiro = 0.20;
    private List<String> movimientos;

    public CuentaCorriente(String numeroCuenta, double saldoInicial) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldoInicial;
        this.movimientos = new ArrayList<>();
        this.movimientos.add("Apertura de Cuenta Corriente: $" + saldoInicial);
    }

    public double getLimiteDisponible() {
        // Permite el saldo actual + el 20% del mismo
        return saldo > 0 ? saldo + (saldo * porcentajeSobregiro) : 0;
    }

    // Getters y Setters
    public String getNumeroCuenta() { return numeroCuenta; }
    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
    public List<String> getMovimientos() { return movimientos; }
}
