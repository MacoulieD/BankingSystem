# Reportes de BankingSystem con Python

El script `reporte_banking.py` consulta el backend REST (no se conecta directamente a MySQL), carga los datos en DataFrames de Pandas, usa NumPy para calcular porcentajes y Matplotlib para crear los gráficos. Los resultados se guardan en `python/reportes/` como CSV y PNG.

## Arquitectura
MySQL → Spring Boot (`/api/reportes/...`) → JSON HTTP → Python `requests` → Pandas + NumPy → Matplotlib (gráficos).

## Endpoints de reportes (solo lectura)
| Método | Endpoint | Datos |
|---|---|---|
| GET | `/api/reportes/cuentas` | número de cuenta, tipo (AH/CC), saldo, sobregiro, estado, fecha de creación |
| GET | `/api/reportes/movimientos` | número de cuenta, tipo, monto, fecha y destino de las transferencias enviadas (banco, billetera o MiPlata) |
| GET | `/api/reportes/tarjetas` | límite, deuda, activa, vencimiento (sin número ni CVV) |
| GET | `/api/reportes/compras-tarjeta` | cuotas, capital, total con intereses, fecha (sin comercio ni datos de la tarjeta) |
| GET | `/api/reportes/personas` | activo/bloqueado e intentos fallidos (sin datos personales) |

## Cómo ejecutarlo
1. Inicia MySQL y el backend (desde la carpeta que tiene `pom.xml`):
   ```powershell
   .\mvnw spring-boot:run
   ```
2. En una segunda terminal, entra a la carpeta `python`:
   ```powershell
   py -m venv .venv
   .venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   python reporte_banking.py
   ```

## Informe en PDF
`reportes/informe_banking.pdf` (lo arma `informe_pdf.py`) reúne todo en un solo documento de 14 páginas. Cada página tiene su gráfica y un texto que explica lo que muestra, con cifras calculadas a partir de los datos:

| Página | Contenido | Gráficas |
|---|---|---|
| 1 | Portada con indicadores generales | Tarjetas de cifras |
| 2 | Cuentas por tipo | Dona + barras |
| 3 | Ranking de cuentas por saldo (números ocultos ****1234) | Barras horizontales |
| 4 | Distribución de saldos | Barras apiladas + cajas (boxplot) |
| 5 | Sobregiro de las cuentas corrientes | Barras agrupadas |
| 6 | Movimientos por tipo | Dona + barras horizontales |
| 7 | Dinero que entra vs dinero que sale por día | Barras agrupadas |
| 8 | Destinos de las transferencias | Dona + barras horizontales |
| 9 | Actividad por día | Línea + barras |
| 10 | Uso del cupo de las tarjetas | Dona + barras por nivel de uso |
| 11 | Compras con tarjeta por número de cuotas | Dona + barras apiladas (capital + intereses) |
| 12 | Costo de financiar una compra (1,8 % mensual) | Barras |
| 13 | Usuarios activos, bloqueados e intentos fallidos | Dona + barras |
| 14 | Conclusiones | Texto |

Si el backend corre en otro puerto, usa la variable `BANKING_API`:
```powershell
$env:BANKING_API = "http://localhost:8081/api/reportes"
python reporte_banking.py
```

## Gráficos generados (PNG)
| Archivo | Qué muestra |
|---|---|
| `cuentas_por_tipo.png` | Ahorro vs Corriente (torta) |
| `saldo_por_tipo.png` | Saldo total por tipo de cuenta |
| `distribucion_saldos.png` | Cuántas cuentas hay en cada rango de saldo |
| `movimientos_por_tipo.png` | Depósitos, retiros, transferencias e intereses |
| `dinero_movido_por_dia.png` | Suma de montos movidos cada día |
| `uso_cupo_tarjetas.png` | Deuda vs límite de cada tarjeta |
| `compras_tarjeta_por_cuotas.png` | Cuántas compras con tarjeta hay en cada plazo (1, 3, 6, 12, 24, 36 cuotas) |
| `intereses_tarjeta_por_cuotas.png` | Capital vs intereses (1,8 % mensual) de las compras en cada plazo |
| `usuarios_por_estado.png` | Usuarios activos vs bloqueados |

Si una tabla está vacía (por ejemplo, todavía no hay movimientos o tarjetas), el script lo avisa y omite esas gráficas.
