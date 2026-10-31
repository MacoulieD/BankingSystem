# Reportes de BankingSystem con Python

El script `reporte_banking.py` consulta el backend REST (no se conecta directamente a MySQL), carga los datos en DataFrames de Pandas, usa NumPy para calcular porcentajes y Matplotlib para crear los gráficos. Los resultados se guardan en `python/reportes/` como CSV y PNG.

## Arquitectura
MySQL → Spring Boot (`/api/reportes/...`) → JSON HTTP → Python `requests` → Pandas + NumPy → Matplotlib (gráficos).

## Endpoints de reportes (solo lectura)
| Método | Endpoint | Datos |
|---|---|---|
| GET | `/api/reportes/cuentas` | número de cuenta, tipo (AH/CC), saldo, estado, fecha de creación |
| GET | `/api/reportes/movimientos` | número de cuenta, tipo, monto, fecha |
| GET | `/api/reportes/tarjetas` | límite, deuda, activa, vencimiento (sin número ni CVV) |
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

## Gráficos generados
| Archivo | Qué muestra |
|---|---|
| `cuentas_por_tipo.png` | Ahorro vs Corriente (torta) |
| `saldo_por_tipo.png` | Saldo total por tipo de cuenta |
| `distribucion_saldos.png` | Cuántas cuentas hay en cada rango de saldo |
| `movimientos_por_tipo.png` | Depósitos, retiros, transferencias e intereses |
| `dinero_movido_por_dia.png` | Suma de montos movidos cada día |
| `uso_cupo_tarjetas.png` | Deuda vs límite de cada tarjeta |
| `usuarios_por_estado.png` | Usuarios activos vs bloqueados |

Si una tabla está vacía (por ejemplo, todavía no hay movimientos o tarjetas), el script lo avisa y omite esas gráficas.
