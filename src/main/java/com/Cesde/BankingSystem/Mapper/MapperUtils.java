package com.Cesde.BankingSystem.Mapper;

// Utilidades compartidas por los mappers y el servicio
public final class MapperUtils {

    private MapperUtils() {
    }

    // null -> "" y quita espacios al inicio y al final
    public static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
