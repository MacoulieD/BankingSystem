package bankingsystem.domain.enums;

public enum EstadoCuenta {
    ACTIVA,
    INACTIVA,
    BLOQUEADA,
    CERRADA;

    public boolean equalsIgnoreCase(String activa) {
        return false;
    }

}
