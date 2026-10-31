-- Ajusta las tablas de dbbankingsystem a las entidades corregidas (MPerson,
-- MCuenta, MMovimiento, MTarjetaCredito) para que coincidan con el formulario de registro.
-- No borra tablas ni datos: solo modifica columnas.
-- Ejecutar UNA vez en phpMyAdmin (pestana SQL) con dbbankingsystem seleccionada,
-- y ANTES de arrancar el backend.

USE dbbankingsystem;

-- las columnas id son referenciadas por llaves foraneas; se desactiva la
-- verificacion mientras se vuelven AUTO_INCREMENT
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE person
  MODIFY idperson INT(11) NOT NULL AUTO_INCREMENT,
  ADD COLUMN tipodocumento VARCHAR(2) NOT NULL AFTER idperson,
  ADD COLUMN documento VARCHAR(10) NOT NULL AFTER tipodocumento,
  MODIFY email VARCHAR(50) NULL,
  MODIFY username VARCHAR(20) NOT NULL,
  MODIFY userpassword VARCHAR(60) NOT NULL,
  ADD CONSTRAINT uk_person_documento UNIQUE (documento),
  ADD CONSTRAINT uk_person_email UNIQUE (email),
  ADD CONSTRAINT uk_person_username UNIQUE (username);

ALTER TABLE cuenta
  MODIFY id INT(11) NOT NULL AUTO_INCREMENT,
  ADD CONSTRAINT uk_cuenta_numerocuenta UNIQUE (numerocuenta);

ALTER TABLE movimientos
  MODIFY id INT(11) NOT NULL AUTO_INCREMENT;

-- la tarjeta de credito tambien genera su id solo (@GeneratedValue en MTarjetaCredito)
ALTER TABLE targeta_credito
  MODIFY id INT(11) NOT NULL AUTO_INCREMENT,
  ADD CONSTRAINT uk_tarjeta_numerotarjeta UNIQUE (numerotarjeta);

SET FOREIGN_KEY_CHECKS = 1;
