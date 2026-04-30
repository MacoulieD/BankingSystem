package bankingsystem.domain;

import java.util.List;

public class TarjetaCredito extends Cuenta {
    private final double cupoTotal;

    public TarjetaCredito(String numeroCuenta, double cupoTotal, String propietario) {
        // Se inicializa con saldo 0 (sin deuda)
        super(numeroCuenta, 0, propietario);
        this.cupoTotal = cupoTotal;
    }

    // --- Lógica de Cupo ---
    public double getCupoTotal() {
        return cupoTotal;
    }

    public double getCupoDisponible() {
        // Cupo disponible = Límite (4M) - Deuda actual (saldo)
        return cupoTotal - getSaldo();
    }

    public double obtenerTasaMensual(int n) {
        if (n <= 2) return 0.0;           // 0%
        if (n <= 6) return 0.019;         // 1.9%
        return 0.023;                     // 2.3%
    }

    /**
     * Calcula la cuota mensual usando la fórmula:
     * Cuota = (Capital * i) / (1 - (1 + i)^-n)
     */
    public double calcularCuotaMensual(double capital, int n) {
        double tasa = obtenerTasaMensual(n);

        if (tasa == 0) return capital / n;

        // Fórmula de amortización francesa
        return (capital * tasa) / (1 - Math.pow(1 + tasa, -n));
    }

    @Override
    public String toString() {
        return String.format("Tarjeta [%s] | Deuda: $%,.2f | Disponible: $%,.2f",
                getNumeroCuenta(), getSaldo(), getCupoDisponible());
    }
    public String getObservacionInteres(int cuotas) {
        if (cuotas <= 2) {
            return "Sin interés (0%)";
        } else if (cuotas <= 6) {
            return "Interés moderado (1.9%)";
        } else {
            return "Interés alto (2.3%)";
        }
    }
    @Override
    public void setMovimientos(List<String> movimientos) {
    }
}