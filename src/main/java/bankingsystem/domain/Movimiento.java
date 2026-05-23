package bankingsystem.domain;

import bankingsystem.domain.enums.TipoMovimiento;
import java.time.LocalDateTime;

public class Movimiento {

    private final int id;
    private final LocalDateTime fechaHora;
    private final TipoMovimiento tipo;
    private final double valor;
    private final double saldoPosterior;
    private final String descripcion;

    public Movimiento(int id, TipoMovimiento tipo, double valor, double saldoPosterior, String descripcion) {
        this.id = id;
        this.fechaHora = LocalDateTime.now();
        this.tipo = tipo;
        this.valor = valor;
        this.saldoPosterior = saldoPosterior;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public TipoMovimiento getTipo() { return tipo; }
    public double getValor() { return valor; }
    public double getSaldoPosterior() { return saldoPosterior; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | $%,.2f | Saldo: $%,.2f",
                id, fechaHora.toString(), tipo, valor, saldoPosterior);
    }
}
