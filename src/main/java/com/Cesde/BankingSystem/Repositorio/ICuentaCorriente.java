package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MCuentaCorriente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio de la entidad MCuentaCorriente (subtipo de MCuenta).
// Por la herencia, MCuentaCorriente tambien tiene los campos de MCuenta (numerocuenta,
// estado, persona...), asi que se puede buscar por ellos. Spring hace el join
// entre cuenta y cuenta_corriente automaticamente.
@Repository
public interface ICuentaCorriente extends JpaRepository<MCuentaCorriente,Integer> {

    // busqueda por numero de cuenta (campo heredado de MCuenta)
    MCuentaCorriente findByNumerocuenta (String Numerocuenta);

    // cuentas de este tipo que pertenecen a una persona
    List<MCuentaCorriente> findByPersona_Idperson (int Idperson);

    // cuentas de este tipo activas o inactivas
    List<MCuentaCorriente> findByEstado (Boolean Estado);
}
