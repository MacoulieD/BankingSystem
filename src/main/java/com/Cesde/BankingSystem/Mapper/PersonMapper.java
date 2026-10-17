package com.Cesde.BankingSystem.Mapper;

import com.Cesde.BankingSystem.Dto.RegistroRequest;
import com.Cesde.BankingSystem.Modelo.MPerson;

// Convierte los datos del formulario de registro en la entidad MPerson.
// Solo copia y normaliza campos: las validaciones se hacen en SPerson antes
// de llamar a este mapper.
public final class PersonMapper {

    private PersonMapper() {
    }

    public static MPerson toEntity(RegistroRequest datos) {
        MPerson persona = new MPerson();
        persona.setTipodocumento(MapperUtils.texto(datos.documentType()));
        persona.setDocumento(MapperUtils.texto(datos.documentNumber()).toUpperCase());
        persona.setName(MapperUtils.texto(datos.fullName()).replaceAll("\\s+", " "));
        persona.setTelephone(MapperUtils.texto(datos.phone()));
        persona.setEmail(MapperUtils.texto(datos.email()).toLowerCase());
        persona.setUsername(MapperUtils.texto(datos.username()).toLowerCase());
        // se guarda tal cual la escribio el usuario (sin cifrar)
        persona.setUserpassword(datos.password());
        persona.setActivo(true);
        persona.setFailedAttempts(0);
        return persona;
    }

    // Perfil: solo copia los datos de contacto editables (ya validados en SPerson)
    public static void actualizarContacto(MPerson persona, String telefono, String email) {
        persona.setTelephone(telefono);
        persona.setEmail(email);
    }
}
