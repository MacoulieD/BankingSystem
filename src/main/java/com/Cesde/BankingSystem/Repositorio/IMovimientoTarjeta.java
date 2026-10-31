package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MMovimientoTarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio de la entidad MMovimientoTarjeta (compras y pagos de una tarjeta)
@Repository
public interface IMovimientoTarjeta extends JpaRepository<MMovimientoTarjeta,Integer> {
    // historial de una tarjeta, del mas antiguo al mas reciente
    List<MMovimientoTarjeta> findByTarjeta_IdOrderByFechaAscIdAsc (int Idtarjeta);

    // al cancelar la tarjeta se borra su historial
    void deleteByTarjeta_Id (int Idtarjeta);
}
