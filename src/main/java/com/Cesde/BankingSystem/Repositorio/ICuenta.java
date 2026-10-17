package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MCuenta;
import com.Cesde.BankingSystem.Modelo.MPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio de la entidad MCuenta (la superclase abstracta de la herencia
// JOINED). Al consultar aqui, Spring Data JPA hace el join con la tabla hija
// correspondiente (cuenta_ahorros o cuenta_corriente) y devuelve el objeto
// concreto (MCuentaAhorro o MCuentaCorriente) segun el discriminador tipocuenta.
@Repository
public interface ICuenta extends JpaRepository<MCuenta,Integer> {
    // busqueda por numero de cuenta: es unico por cuenta
    MCuenta findByNumerocuenta (String Numerocuenta);
    // para no generar dos cuentas con el mismo numero al registrar
    boolean existsByNumerocuenta (String Numerocuenta);
    // cuenta principal de una persona (la que se crea en el registro)
    MCuenta findFirstByPersonaOrderByIdAsc (MPerson Persona);

    // todas las cuentas de una persona (puede tener varias)
    List<MCuenta> findByPersona_Idperson (int Idperson);

    // cuentas activas o inactivas
    List<MCuenta> findByEstado (Boolean Estado);
}
