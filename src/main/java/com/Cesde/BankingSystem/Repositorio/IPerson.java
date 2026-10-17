package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio de la entidad MPerson: al extender JpaRepository ya trae gratis
// las operaciones basicas (save, findById, findAll, deleteById, etc.), sin
// necesidad de escribir SQL. Spring Data JPA implementa esta interfaz en tiempo
// de ejecucion.
@Repository
public interface IPerson extends JpaRepository<MPerson,Integer> {
    // busqueda por el nombre: puede haber varias personas con el mismo nombre,
    // por eso devuelve una lista
    List<MPerson> findByName (String Name);
    // busqueda por el username (login): el username deberia ser unico,
    // por eso devuelve un solo registro
    MPerson findByUsername (String Username);
    // validaciones del registro: username, documento y email no se pueden repetir
    boolean existsByUsername (String Username);
    boolean existsByDocumento (String Documento);
    boolean existsByEmail (String Email);
    // al editar el perfil: el correo no puede ser de OTRA persona
    boolean existsByEmailAndIdpersonNot (String Email, int Idperson);
}
