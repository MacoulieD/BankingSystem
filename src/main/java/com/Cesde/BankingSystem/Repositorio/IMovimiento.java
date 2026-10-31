package com.Cesde.BankingSystem.Repositorio;

import com.Cesde.BankingSystem.Modelo.MMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// Repositorio de la entidad MMovimiento (el historial de transacciones de
// una cuenta: depositos, retiros, transferencias, etc.).
@Repository
public interface IMovimiento extends JpaRepository<MMovimiento,Integer> {
    // busqueda por fecha: para filtrar movimientos de un dia/momento puntual
    List<MMovimiento> findByFecha (LocalDateTime Fecha);
    // busqueda por numero de cuenta: para ver el extracto/historial de una cuenta
    List<MMovimiento> findByNumerocuenta (String Numerocuenta);

    // historial de una cuenta por su id, del mas reciente al mas antiguo
    List<MMovimiento> findByCuenta_IdOrderByFechaDesc (int Idcuenta);
    // historial de una cuenta del mas antiguo al mas reciente (como lo recibe el frontend)
    List<MMovimiento> findByCuenta_IdOrderByFechaAscIdAsc (int Idcuenta);

    // movimientos entre dos fechas (ej. extracto de un mes)
    List<MMovimiento> findByFechaBetween (LocalDateTime Desde, LocalDateTime Hasta);
}
