package bankingsystem.domain;
// Importamos el enum para definir el tipo de cuenta
import bankingsystem.domain.enums.TypoCuenta;
import java.util.List;
// La clase TarjetaCredito representa una cuenta de crédito con un cupo total y una tasa de interés variable según el número de cuotas.
public class TarjetaCredito extends Cuenta {
    private final double cupoTotal;
// El constructor inicializa la tarjeta con un número de cuenta, un cupo total y el propietario. El saldo inicial es 0 (sin deuda).
    public TarjetaCredito(String numeroCuenta, double cupoTotal, String propietario) {
        // Se inicializa con saldo 0 (sin deuda)
        super(numeroCuenta, 0, propietario);
        this.cupoTotal = cupoTotal;
        this.tipo = TypoCuenta.TARJETA_CREDITO;
    }

    // --- Lógica de Cupo ---
    public double getCupoTotal() {
        return cupoTotal;
    }
// El cupo disponible se calcula restando la deuda actual (saldo) del cupo total.
    public double getCupoDisponible() {
        // Cupo disponible = Límite (4M) - Deuda actual (saldo)
        return cupoTotal - getSaldo();
    }
// La tasa de interés mensual varía según el número de cuotas: 0% para 1-2 cuotas, 1.9% para 3-6 cuotas, y 2.3% para más de 6 cuotas.
    public double obtenerTasaMensual(int n) {
        if (n <= 2) return 0.0;           // 0%
        if (n <= 6) return 0.019;         // 1.9%
        return 0.023;                     // 2.3%
    }
// El método calcularCuotaMensual utiliza la fórmula de amortización francesa para calcular el valor de cada cuota mensual en función del capital (monto de la compra), la tasa de interés y el número de cuotas.
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
// El método toString se sobrescribe para mostrar la información relevante de la tarjeta, incluyendo el número de cuenta, la deuda actual y el cupo disponible.
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