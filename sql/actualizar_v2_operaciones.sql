-- Segunda actualizacion de dbbankingsystem: deja la BD lista para que el
-- frontend guarde TODAS las operaciones (consignar, retirar, transferir,
-- varias cuentas y tarjeta de credito con compras a cuotas).
-- No borra tablas ni datos: solo agrega restricciones y una tabla nueva.
-- Ejecutar UNA vez en phpMyAdmin (pestana SQL) con dbbankingsystem
-- seleccionada, DESPUES de actualizar_dbbankingsystem.sql y ANTES de
-- arrancar el backend.

USE dbbankingsystem;

-- 1. El celular identifica al destinatario de las transferencias por
--    celular: no se puede repetir.
ALTER TABLE person
  ADD CONSTRAINT uk_person_telephone UNIQUE (telephone);

-- 2. La tarjeta genera su id sola (@GeneratedValue en MTarjetaCredito) y su
--    numero no se puede repetir.
ALTER TABLE targeta_credito
  MODIFY id INT(11) NOT NULL AUTO_INCREMENT,
  ADD CONSTRAINT uk_tarjeta_numerotarjeta UNIQUE (numerotarjeta);

-- 3. Historial de la tarjeta (entidad MMovimientoTarjeta): compras a cuotas
--    y pagos. capital, cuotas, valorcuota y saldopendiente solo se llenan en
--    las compras. IF NOT EXISTS: si el backend ya arranco con el codigo nuevo,
--    Hibernate (ddl-auto=update) pudo haberla creado antes.
CREATE TABLE IF NOT EXISTS movimiento_tarjeta (
  id INT(11) NOT NULL AUTO_INCREMENT,
  pktarjeta INT(11) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  monto DOUBLE NOT NULL,
  deudaposterior DOUBLE NOT NULL,
  fecha DATETIME(6) NOT NULL,
  capital DOUBLE NULL,
  cuotas INT(11) NULL,
  valorcuota DOUBLE NULL,
  saldopendiente DOUBLE NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_movtarjeta_tarjeta FOREIGN KEY (pktarjeta) REFERENCES targeta_credito (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
