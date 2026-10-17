package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MTarjetaCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio de la entidad MTarjetaCredito (tarjetas asociadas a una cuenta).
@Repository
public interface ITarjetaCredito extends JpaRepository<MTarjetaCredito,Integer> {
    // busqueda por numero de tarjeta: es unico por tarjeta
    MTarjetaCredito findByNumerotarjeta (String Numerotarjeta);
    // busqueda por estado (activa/inactiva): para listar solo las tarjetas vigentes
    List<MTarjetaCredito> findByActivo (Boolean Activo);

    // tarjetas asociadas a una cuenta
    List<MTarjetaCredito> findByCuenta_Id (int Idcuenta);
}
