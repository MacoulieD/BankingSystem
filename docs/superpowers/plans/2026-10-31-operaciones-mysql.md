# Operaciones del frontend en MySQL — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Que todas las operaciones del frontend MiPlata (cuentas, consignar/retirar/transferir, tarjeta con cuotas) se guarden en MySQL a través del backend.

**Architecture:** Nuevos controladores `CCuenta`, `CMovimiento`, `CTarjeta` sobre servicios existentes (`SCuenta`, `SMovimiento`, `STarjetaCredito`) ampliados con operaciones "por usuario"; una tabla nueva `movimiento_tarjeta`; `CuentaResponse` pasa a "persona + cuentas + tarjeta". El frontend cambia a `USE_LOCAL_STORAGE = false` y reemplaza su estado con lo que devuelve el backend.

**Tech Stack:** Spring Boot 4.1 / Java 21 / JPA / MySQL (XAMPP); React 19 + Vite; Python (reportes).

**Spec:** `docs/superpowers/specs/2026-10-31-operaciones-mysql-design.md`

## Global Constraints

- No se cambian las rutas ni contratos de `/api/registro`, `/api/login`, `/api/perfil/*`, `/api/reportes/*` salvo la forma de `CuentaResponse` (sección 4 del spec).
- Estilo: prefijos `C*`/`S*`/`I*`/`M*`, DTOs `record`, `ExcepcionNegocio(HttpStatus, mensaje)`, `try/catch` con relanzamiento, comentarios en español sin tildes en Java.
- `@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})` en cada controlador nuevo.
- Constantes: cupo 500.000–20.000.000; cuotas 1–36; sin interés hasta 3; interés 0,019 mensual; vence a 5 años; tarjeta 16 dígitos empezando por 4; CVV 3 dígitos.
- Nunca enviar contraseña ni CVV; `cardNumber` enmascarado (`"************" + últimos 4`).
- Pruebas contra `dbbankingsystem_prueba` (copia), nunca contra `dbbankingsystem`.
- El proyecto no es repositorio git: no hay pasos de commit.

## Review Focus

- Operar sobre una cuenta/tarjeta de **otro** usuario (username A, número de cuenta de B) → 403 "La cuenta no pertenece al usuario." — probado en Task 4 (`curl` consignar con cuenta ajena).
- Montos con decimales o negativos (`10.5`, `-1`) → 400 "Ingresa un monto entero mayor que cero." — Task 4.
- Pago de tarjeta mayor a la deuda o al saldo de la cuenta → 400/409 sin cambiar nada (rollback) — Task 5.
- Transferencia por celular al propio celular → 400 — Task 4.
- Frontend con backend apagado → mensaje "No se pudo conectar con el servidor..." y el saldo en pantalla no cambia — Task 7.

---

### Task 1: Base de datos y entidad del historial de tarjeta

**Files:**
- Create: `sql/actualizar_v2_operaciones.sql`
- Create: `src/main/java/com/Cesde/BankingSystem/Modelo/MMovimientoTarjeta.java`
- Create: `src/main/java/com/Cesde/BankingSystem/Repositorio/IMovimientoTarjeta.java`
- Modify: `Modelo/MPerson.java` (`telephone` `unique = true`), `Modelo/MTarjetaCredito.java` (`numerotarjeta` `unique = true`, relación `@OneToMany movimientos`)

**Interfaces:**
- Produces: `MMovimientoTarjeta` (campos del spec §3: id, tarjeta, tipo, descripcion, monto, deudaposterior, fecha, capital, cuotas, valorcuota, saldopendiente); `IMovimientoTarjeta.findByTarjeta_IdOrderByFechaAscIdAsc(int)`, `deleteByTarjeta_Id(int)`; `IPerson.findByTelephone(String)`, `existsByTelephone(String)`, `existsByTelephoneAndIdpersonNot(String,int)`.

- [ ] Crear copia: `CREATE DATABASE dbbankingsystem_prueba` + `mysqldump dbbankingsystem | mysql dbbankingsystem_prueba`.
- [ ] Escribir el script (UNIQUE telephone, AUTO_INCREMENT + UNIQUE en targeta_credito, CREATE TABLE movimiento_tarjeta con FK).
- [ ] Aplicarlo a la copia; verificar con `SHOW CREATE TABLE`.
- [ ] Entidad + repositorio; `mvnw compile` OK.

### Task 2: Nueva forma de `CuentaResponse`

**Files:**
- Modify: `Dto/CuentaResponse.java` → `(documentType, documentNumber, phone, email, fullName, username, List<CuentaBancariaResponse> bankAccounts, TarjetaResponse creditCard)`
- Create: `Dto/CuentaBancariaResponse.java` `(accountType, accountNumber, balance, List<MovimientoResponse> transactions)`
- Create: `Dto/TarjetaResponse.java` `(cardNumber, expiresAt, limit, debt, active, accountNumber, List<MovimientoTarjetaResponse> transactions)`
- Create: `Dto/MovimientoTarjetaResponse.java` `(id, type, description, amount, debtAfter, date, capital, installments, monthlyPayment, remaining)`
- Modify: `Mapper/CuentaMapper.java` (`toResponse(MPerson, List<MCuenta>, MTarjetaCredito, ...)`), `Mapper/MovimientoMapper.java`
- Create: `Mapper/TarjetaMapper.java`
- Modify: `Servicio/SPerson.java` (`armarRespuesta(MPerson)` público; registro/contacto validan celular único)

**Interfaces:**
- Produces: `SPerson.armarRespuesta(MPerson persona): CuentaResponse` (cuentas por id asc, movimientos por fecha asc, tarjeta de cualquiera de sus cuentas o `null`); `SPerson.buscarPersona(String username): MPerson` (paquete-visible o público).

- [ ] Implementar; compilar; `curl POST /api/login` sobre la copia devuelve `bankAccounts[0].accountNumber` y `creditCard: null`.

### Task 3: Cuentas — `CCuenta`

**Files:** Create `Controlador/CCuenta.java`, `Dto/AbrirCuentaRequest.java (username, accountType, balance Long)`, `Dto/DestinoResponse.java (fullName, username, accountType, accountNumber)`; Modify `Servicio/SCuenta.java`.

**Interfaces:**
- Produces: `SCuenta.abrirCuenta(AbrirCuentaRequest): CuentaResponse`, `SCuenta.buscarDestino(String numero): DestinoResponse`, `SCuenta.buscarDestinoPorCelular(String celular): DestinoResponse`, `SCuenta.cuentaDelUsuario(MPerson, String numero): MCuenta` (403 si no es suya).
- [ ] `curl`: abrir CC → 200 con 2 cuentas; abrir CC otra vez → 409 "Ya tienes una Cuenta Corriente."; destino inexistente → 404.

### Task 4: Movimientos — `CMovimiento`

**Files:** Create `Controlador/CMovimiento.java`, `Dto/OperacionRequest.java (username, accountNumber, amount Long)`, `Dto/TransferenciaRequest.java (username, fromAccountNumber, amount Long, destinationType, bank, destination)`; Modify `Servicio/SMovimiento.java`.

**Interfaces:**
- Produces: `SMovimiento.consignar(OperacionRequest)`, `retirar(OperacionRequest)`, `transferir(TransferenciaRequest)` → `CuentaResponse`; tipo nuevo `PAGO_TARJETA = "card-payment"`.
- [ ] `curl`: consignar, retirar (y retirar de más → 409), transferir por celular, por cuenta propia, por cuenta ajena con tipo equivocado (400), a Bancolombia; cuenta ajena → 403; monto decimal/negativo → 400; propio celular → 400. Verificar filas en `movimientos`.

### Task 5: Tarjeta — `CTarjeta`

**Files:** Create `Controlador/CTarjeta.java`, `Dto/SolicitarTarjetaRequest (username, accountNumber, limit Long)`, `Dto/CompraTarjetaRequest (username, merchant, amount Long, installments Integer)`, `Dto/PagoTarjetaRequest (username, fromAccountNumber, amount Long)`, `Dto/UsuarioRequest (username)`; Modify `Servicio/STarjetaCredito.java`.

**Interfaces:** `STarjetaCredito.solicitar`, `comprar`, `pagar`, `bloquear`, `cancelar` → `CuentaResponse`. Plan de cuotas idéntico a `installmentPlan` de `creditCard.js` (techo, amortización francesa con i = 0,019 desde 4 cuotas).
- [ ] `curl`: solicitar (cupo 400.000 → 400), compra 1 cuota 100.000 (deuda 100.000), compra 6 cuotas 140.000 (cuota = techo(140000·0,019/(1−1,019^−6)) = 24.938, total 149.628), pago mayor a deuda → 400, pago 50.000 (remaining de la primera compra 50.000), bloquear, compra con bloqueada → 409, cancelar con deuda → 409, pagar todo, cancelar → `creditCard: null`.

### Task 6: Reportes Python

**Files:** Modify `python/reporte_banking.py` (`TIPOS_MOVIMIENTO["card-payment"] = "Pago de tarjeta"`).
- [ ] Ejecutar contra el backend de la copia: 7 gráficas.

### Task 7: Frontend

**Files:** Modify `src/modules/core/api/api.js`, `backendApi.js`, `localApi.js`, `src/App.jsx`, `modules/dashboard/DashboardPage.jsx`, `modules/transfers/TransferPanel.jsx`, `modules/credit-card/CreditCardPanel.jsx`, `DOCUMENTACION.md`.

**Interfaces:** funciones de API (mismo nombre en ambos modos): `consignar(username, accountNumber, amount)`, `retirar(...)`, `transferir(username, fromAccountNumber, {destinationType, bank, destination}, amount)`, `buscarCuentaDestino(numero)`, `buscarDestinoPorCelular(celular)`, `solicitarTarjeta(username, accountNumber, limit)`, `comprarConTarjeta(username, merchant, amount, installments)`, `pagarTarjeta(username, fromAccountNumber, amount)`, `bloquearTarjeta(username)`, `cancelarTarjeta(username)`, `abrirCuenta(username, accountType, balance)`. Todas devuelven la cuenta pública (salvo las de búsqueda).
- [ ] `npm run build` y `npm run lint` sin errores nuevos.
- [ ] Flujo completo en el navegador contra la copia; verificar filas en MySQL.

### Task 8: Cierre

- [ ] Aplicar `sql/actualizar_v2_operaciones.sql` a `dbbankingsystem` (la real) solo después de que todo pase.
- [ ] Borrar `dbbankingsystem_prueba`; devolver `application.properties` a `dbbankingsystem`.
