package bankingsystem.domain;

import java.util.ArrayList;
import java.util.List;

public class TarjetaCredito {
    private String numeroTarjeta;
    private final double cupoMaximo = 4000000.0;
    private double deudaActual;
    private List<String> movimientos;

    public TarjetaCredito(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
        this.deudaActual = 0;
        this.movimientos = new ArrayList<>();
        this.movimientos.add("Activación de Tarjeta de Crédito. Cupo: $4.000.000");
    }

    public double getCupoDisponible() {
        return cupoMaximo - deudaActual;
    }

    // Lógica de intereses según cuotas
    public double calcularInteres(int cuotas) {
        if (cuotas <= 2) return 0.0;
        if (cuotas <= 6) return 0.019; // 1.9%
        return 0.023; // 2.3%
    }

    // Getters y Setters
    public String getNumeroTarjeta() { return numeroTarjeta; }
    public double getDeudaActual() { return deudaActual; }
    public void setDeudaActual(double deudaActual) { this.deudaActual = deudaActual; }
    public List<String> getMovimientos() { return movimientos; }
}