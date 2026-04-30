package bankingsystem.domain.enums;

public enum TypoCuenta {
    AHORROS(1, "Cuenta de Ahorros"),
    CORRIENTE(2, "Cuenta Corriente"),
    TARJETA_CREDITO(3, "Tarjeta de Crédito"); // Nueva opción añadida

    private final int id;
    private final String nombre;

    TypoCuenta(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public static TypoCuenta desdeId(int id) {
        for (TypoCuenta tipo : values()) {
            if (tipo.id == id) {
                return tipo;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return nombre;
    }
}