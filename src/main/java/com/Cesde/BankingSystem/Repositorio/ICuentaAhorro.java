package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MCuentaAhorro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio de la entidad MCuentaAhorro (subtipo de MCuenta).
// Por la herencia, MCuentaAhorro tambien tiene los campos de MCuenta (numerocuenta,
// estado, persona...), asi que se puede buscar por ellos. Spring hace el join
// entre cuenta y cuenta_ahorros automaticamente.
@Repository
public interface ICuentaAhorro extends JpaRepository<MCuentaAhorro,Integer> {

    // busqueda por numero de cuenta (campo heredado de MCuenta)
    MCuentaAhorro findByNumerocuenta (String Numerocuenta);

    // cuentas de este tipo que pertenecen a una persona
    List<MCuentaAhorro> findByPersona_Idperson (int Idperson);

    // cuentas de este tipo activas o inactivas
    List<MCuentaAhorro> findByEstado (Boolean Estado);
}
