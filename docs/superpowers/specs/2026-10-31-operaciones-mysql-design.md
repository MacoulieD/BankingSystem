# Diseño: todas las operaciones del frontend guardadas en MySQL

Fecha: 2026-10-31
Proyectos: `BankingSystem-main` (backend Spring Boot) y `BankingSystemFrontd-develop` (frontend React "MiPlata").

## 1. Intención

Hoy el frontend tiene `USE_LOCAL_STORAGE = true` (`src/modules/core/api/api.js`): usuarios, saldos, movimientos y tarjeta viven en el `localStorage` del navegador. El backend solo soporta registro, login y perfil.

Objetivo: que **todo lo que hace el frontend quede guardado en MySQL (`dbbankingsystem`)** a través del backend, para que:
- los datos no dependan del navegador, y
- el script `python/reporte_banking.py` genere las 7 gráficas con datos reales (movimientos y tarjetas incluidos).

Decisión del usuario: el backend debe soportar **todo** lo que ya hace el frontend (varias cuentas, transferencias por celular y a otros bancos, tarjeta con compras a cuotas con interés), se puede modificar la base de datos y el frontend deja de guardar en local.

## 2. Metas, no-metas y anti-metas

**Metas**
- Paridad funcional con el modo local del frontend (sección 5).
- Mismas reglas de negocio en el backend que hoy aplica el frontend (el backend es la fuente de verdad).
- No romper registro, login, perfil ni los endpoints `/api/reportes`.
- Mantener la estructura de carpetas y el estilo existente (prefijos `C*`, `S*`, `I*`, `M*`, DTOs `record`, `ExcepcionNegocio`, `try/catch` del profe, comentarios en español).

**No-metas (se dejan como están)**
- Autenticación con tokens/JWT: el usuario se sigue identificando por el `username` que envía el frontend.
- Cifrado de contraseñas, `BigDecimal` en lugar de `Double`, bloqueo de concurrencia.
- Migrar a MySQL los usuarios que hoy están en el `localStorage` de algún navegador.

**Anti-metas (sería un fracaso aunque "funcione")**
- Que una operación se vea exitosa en pantalla pero no quede en MySQL (o al revés).
- Que el frontend calcule saldos o deudas por su cuenta: siempre muestra lo que devuelve el backend.
- Exponer en las respuestas la contraseña o el CVV.

## 3. Cambios en la base de datos

Nuevo script `sql/actualizar_v2_operaciones.sql` (se ejecuta una vez en phpMyAdmin, antes de arrancar el backend; no borra datos):

1. `person.telephone` → `UNIQUE` (el celular identifica al destinatario en transferencias). Verificado: hoy no hay celulares repetidos.
2. `targeta_credito.id` → `AUTO_INCREMENT` y `numerotarjeta` → `UNIQUE` (esa parte del script anterior no quedó aplicada; sin esto, crear una tarjeta falla).
3. Tabla nueva `movimiento_tarjeta` (historial de la tarjeta), entidad `MMovimientoTarjeta`:

| Columna | Tipo | Uso |
|---|---|---|
| `id` | int AUTO_INCREMENT PK | |
| `pktarjeta` | int FK → `targeta_credito.id` | |
| `tipo` | varchar(20) | `card-purchase` o `card-payment` |
| `descripcion` | varchar(255) | "Compra en X" / "Pago desde cuenta ****1234" |
| `monto` | double | total con intereses (compra) o valor pagado (pago) |
| `deudaposterior` | double | deuda de la tarjeta después del movimiento |
| `fecha` | datetime | |
| `capital` | double NULL | solo compras: valor sin intereses |
| `cuotas` | int NULL | solo compras: 1 a 36 |
| `valorcuota` | double NULL | solo compras: cuota mensual |
| `saldopendiente` | double NULL | solo compras: lo que falta pagar de esa compra |

La tabla `movimientos` (cuentas) no cambia: ya admite los tipos `deposit`, `withdrawal`, `transfer-out`, `transfer-in`, `interest`; se agrega el tipo `card-payment` (cabe en varchar(20)).

`spring.jpa.hibernate.ddl-auto=update` crearía la tabla nueva solo, pero el script SQL la crea igual para que quede documentado y para aplicar los cambios 1 y 2, que Hibernate no hace sobre columnas existentes.

## 4. Formato de la respuesta (lo que recibe el frontend)

Todas las operaciones devuelven **la cuenta completa actualizada del usuario**, y el frontend reemplaza su copia en memoria con ella. `CuentaResponse` cambia de "una cuenta" a "persona + cuentas + tarjeta" (el frontend ya entiende esta forma: es la de `toPublicAccount` en `localApi.js`):

```json
{
  "documentType": "CC", "documentNumber": "1092834586", "phone": "3007364523",
  "email": "a@b.com", "fullName": "Nombre Apellido", "username": "usuario",
  "bankAccounts": [
    { "accountType": "AH", "accountNumber": "8242751185", "balance": 50000.0,
      "transactions": [ { "id": "12", "type": "deposit", "description": "Consignación",
                          "amount": 10000.0, "balanceAfter": 50000.0, "date": "2026-10-31T10:00:00" } ] }
  ],
  "creditCard": null
}
```

`creditCard`, cuando existe:
```json
{ "cardNumber": "************3456", "expiresAt": "2031-10-31", "limit": 2000000.0, "debt": 150000.0,
  "active": true, "accountNumber": "8242751185",
  "transactions": [ { "id": "3", "type": "card-purchase", "description": "Compra en Éxito",
                      "amount": 150000.0, "debtAfter": 150000.0, "date": "...",
                      "capital": 140000.0, "installments": 6, "monthlyPayment": 25000.0, "remaining": 150000.0 } ] }
```
- `cardNumber` va **enmascarado** (solo los últimos 4 dígitos reales; mismo largo de 16). El CVV **nunca** se envía.
- Los movimientos van del más antiguo al más reciente (el frontend los invierte para mostrarlos), igual que en modo local.
- `POST /api/login`, `POST /api/registro` y `PUT /api/perfil/contacto` también devuelven esta forma.

## 5. Funcionalidades y reglas (paridad con el modo local)

Endpoints nuevos (controladores nuevos con el mismo estilo de `CAutenticacion`/`CPerfil`, `@CrossOrigin` para Vite). Todos reciben el `username` y el backend **verifica que la cuenta o tarjeta pertenezca a ese usuario**.

### Cuentas — `CCuenta`, `/api/cuentas`
| Endpoint | Cuerpo / parámetros | Regla |
|---|---|---|
| `POST /api/cuentas` | `username, accountType (AH/CC), balance` | Abre otra cuenta. Máximo una de cada tipo por usuario ("Ya tienes una Cuenta de Ahorros."). Saldo inicial entero ≥ 0. Ahorros tasa 0,5; corriente sobregiro 0 (igual que el registro). |
| `GET /api/cuentas/destino?numero=` | | Para validar una transferencia a cuenta MiPlata: devuelve `fullName, username, accountType, accountNumber` o 404 "No encontramos una cuenta MiPlata con ese número." |
| `GET /api/cuentas/destino-celular?celular=` | | Igual, buscando al usuario por celular; devuelve su **primera** cuenta (la más antigua). 404 "No hay una cuenta MiPlata registrada con ese celular." |

### Movimientos — `CMovimiento`, `/api/movimientos`
| Endpoint | Cuerpo | Regla |
|---|---|---|
| `POST /consignar` | `username, accountNumber, amount` | Entero > 0. Movimiento `deposit` "Consignación". |
| `POST /retirar` | `username, accountNumber, amount` | Entero > 0, no mayor al saldo. Movimiento `withdrawal` "Retiro". |
| `POST /transferir` | `username, fromAccountNumber, amount, destinationType (phone/AH/CC), bank, destination` | Ver abajo. |

Transferencia (mismos casos de `TransferPanel.jsx`, mismas descripciones de `App.jsx`):
- `bank = "MiPlata"` + `phone`: busca por celular; no puede ser el propio celular. Origen `transfer-out` "Transferencia a {username}", destino (primera cuenta) `transfer-in` "Transferencia de {username}". Una sola transacción (`@Transactional`).
- `bank = "MiPlata"` + `AH`/`CC`: número de 10 dígitos, distinto al origen, debe existir y **coincidir el tipo** ("El número no corresponde a una cuenta de ahorros."). Puede ser otra cuenta del mismo usuario ("Transferencia a tu cuenta ****1234" / "Transferencia desde tu cuenta ****5678").
- Otro banco o Nequi/Daviplata: no se valida el destino (celular de 10 dígitos que empieza por 3, o cuenta de 6 a 20 dígitos); solo se descuenta al origen con `transfer-out` "Transferencia a {banco} ****1234". Lista de bancos permitidos igual a la del frontend.
- Monto entero > 0 y no mayor al saldo de la cuenta origen (las cuentas corrientes tienen sobregiro 0, así que en la práctica es el saldo).

### Tarjeta de crédito — `CTarjeta`, `/api/tarjeta`
| Endpoint | Cuerpo | Regla |
|---|---|---|
| `POST /solicitar` | `username, accountNumber, limit` | Una tarjeta por usuario ("Ya tienes una tarjeta de crédito."), ligada a una de sus cuentas. Cupo entero entre 500.000 y 20.000.000. Número de 16 dígitos que empieza por 4, único; CVV aleatorio de 3 dígitos; vence en 5 años; deuda 0; activa. |
| `POST /compra` | `username, merchant, amount, installments` | Tarjeta activa y no vencida. Comercio ≥ 2 caracteres (máx. 40). Monto entero > 0. Cuotas 1 a 36. Plan: hasta 3 cuotas sin interés (cuota = techo(capital/cuotas), total = capital); desde 4 cuotas interés 1,9 % mensual con cuota fija (cuota = techo(capital·i / (1 − (1+i)^−n)), total = cuota·n). El **total** no puede superar el cupo disponible (límite − deuda). La deuda sube por el total. Movimiento `card-purchase` con `saldopendiente = total`. |
| `POST /pago` | `username, fromAccountNumber, amount` | Entero > 0, no mayor a la deuda ni al saldo de la cuenta. Se permite aunque la tarjeta esté bloqueada o vencida. Descuenta de la cuenta (movimiento de cuenta `card-payment` "Pago tarjeta de crédito ****3456"), resta la deuda (movimiento de tarjeta `card-payment` "Pago desde cuenta ****1234") y abona al `saldopendiente` de las compras de la más antigua a la más reciente. Todo en una transacción. |
| `PUT /bloquear` | `username` | `activo = false`. No se puede desbloquear. |
| `POST /cancelar` | `username` | Solo sin deuda ("La tarjeta tiene deuda pendiente: no se puede cancelar."). Se elimina la tarjeta y su historial (el frontend dice "se elimina definitivamente"); después puede solicitar otra. |

La "cuota del mes" (`monthlyDue`) la sigue calculando el frontend con los `transactions` de la tarjeta (`monthlyPayment` y `remaining`); el backend solo tiene que enviarlos.

### Perfil
`PUT /api/perfil/contacto` además valida que el celular no sea de otro usuario ("Ese número de celular ya está registrado por otro usuario."). El registro valida lo mismo ("Ese número de celular ya está registrado.").

### Reportes Python
`reporte_banking.py` agrega el nombre legible de `card-payment` ("Pago de tarjeta"). Los endpoints `/api/reportes` no cambian.

## 6. Cambios en el frontend (`BankingSystemFrontd-develop`)

- `api.js`: `USE_LOCAL_STORAGE = false`. Se conserva `localApi.js` y el interruptor (decisión mía, ver sección 8).
- `backendApi.js`: implementar `abrirCuenta`, `solicitarTarjeta`, `cancelarTarjeta` y agregar `consignar`, `retirar`, `transferir`, `buscarCuentaDestino`, `buscarDestinoPorCelular`, `comprarConTarjeta`, `pagarTarjeta`, `bloquearTarjeta`. `localApi.js` expone las mismas funciones para que el interruptor siga funcionando.
- `App.jsx`: en modo base de datos, las operaciones llaman a la API y reemplazan la cuenta del usuario con la respuesta (en lugar de calcular saldo/deuda en memoria). Pasan a ser `async` y devuelven la promesa.
- `DashboardPage.jsx`, `TransferPanel.jsx`, `CreditCardPanel.jsx`: esperan la respuesta (`await`), muestran "Procesando..." en el botón y muestran el mensaje de error del backend si falla. Las validaciones de formulario que ya existen se mantienen (el backend las repite).
- `TransferPanel.jsx`: para destinos MiPlata consulta al backend (`/api/cuentas/destino...`) en lugar de buscar en las cuentas en memoria, y muestra el nombre del destinatario en la confirmación como hoy.
- `DOCUMENTACION.md`: actualizar las secciones de API, procesos y limitaciones.

## 7. Verificación

- Backend: `mvnw compile`, arranque contra MySQL y prueba de cada endpoint con `curl` (casos exitosos y errores de regla), contra una **copia** de la base (`dbbankingsystem_prueba`) para no ensuciar los datos reales; al final se borra la copia.
- Frontend: `npm run build` y `npm run lint` sin errores nuevos; prueba manual del flujo completo (registro → abrir segunda cuenta → consignar → retirar → transferir por celular y por cuenta → otro banco → tarjeta: solicitar, comprar a 1 y a 6 cuotas, pagar cuota del mes, bloquear, pagar todo, cancelar) y comprobar en MySQL que cada paso quedó guardado.
- Python: ejecutar `reporte_banking.py` y confirmar que salen las 7 gráficas.

## 8. Decisiones tomadas por el constructor (revisar)

1. **Cancelar tarjeta borra la tarjeta y su historial** de la base (como dice el frontend). Consecuencia: esas compras dejan de aparecer en las gráficas.
2. **Se conserva el modo local** (`localApi.js` y el interruptor en `false`) en vez de borrarlo, por si se quiere hacer una demostración sin backend.
3. **El número de tarjeta se envía enmascarado** y el CVV nunca se envía.
4. **Las búsquedas de destino devuelven el nombre completo** del titular (necesario para la confirmación de la transferencia, como hacen Nequi o los bancos).
5. **Las pruebas se hacen sobre una copia de la base**, no sobre `dbbankingsystem`.
6. Los usuarios que existan solo en el `localStorage` de un navegador no se migran; hay que registrarlos de nuevo.
