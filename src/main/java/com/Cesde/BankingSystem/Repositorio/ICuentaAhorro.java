package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MCuentaAhorro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repositorio de la entidad MCuentaAhorro (subtipo de MCuenta).
// Todavia no tiene metodos de busqueda propios porque tasainteres no es un
// campo por el que normalmente se busque una cuenta; con save/findById/
// findAll heredados de JpaRepository es suficiente por ahora.
@Repository
public interface ICuentaAhorro extends JpaRepository<MCuentaAhorro,Integer> {
}
