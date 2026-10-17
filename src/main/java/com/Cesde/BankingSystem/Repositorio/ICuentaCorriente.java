package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MCuentaCorriente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repositorio de la entidad MCuentaCorriente (subtipo de MCuenta).
// Igual que ICuentaAhorro, por ahora solo usa las operaciones basicas
// heredadas de JpaRepository (sobregiro tampoco es un campo de busqueda comun).
@Repository
public interface ICuentaCorriente extends JpaRepository<MCuentaCorriente,Integer> {
}
