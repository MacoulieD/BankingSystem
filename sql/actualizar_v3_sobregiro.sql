-- Tercera actualizacion de dbbankingsystem: la cuenta corriente tiene un
-- cupo de sobregiro del 20% del saldo con que se abrio (CuentaMapper.PORCENTAJE_SOBREGIRO).
-- Las cuentas nuevas ya lo reciben al crearse; este script se lo da a las
-- cuentas corrientes que se crearon antes con sobregiro 0.
-- El saldo de apertura es el saldo anterior de su primer movimiento, o el
-- saldo actual si la cuenta todavia no tiene movimientos.
-- No borra datos. Ejecutar UNA vez en phpMyAdmin (pestana SQL).

USE dbbankingsystem;

UPDATE cuenta_corriente cc
JOIN cuenta c ON c.id = cc.id
SET cc.sobregiro = FLOOR(0.20 * GREATEST(0, COALESCE(
      (SELECT m.saldoanterior FROM movimientos m
        WHERE m.pkcuenta = c.id
        ORDER BY m.fecha, m.id
        LIMIT 1),
      c.saldo)))
WHERE cc.sobregiro = 0;
