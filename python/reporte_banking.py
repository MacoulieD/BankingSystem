"""Consume la API REST de BankingSystem (/api/reportes) y crea tablas CSV y
gráficos con Pandas, NumPy y Matplotlib.

Requiere que el backend Spring Boot esté ejecutándose en el puerto 8080.
"""
from pathlib import Path
import sys
import requests
import pandas as pd
import numpy as np
import matplotlib
matplotlib.use("Agg")  # solo se guardan imágenes PNG, no se abren ventanas
import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator, StrMethodFormatter

BASE_URL = "http://localhost:8080/api/reportes"
SALIDA = Path(__file__).resolve().parent / "reportes"
SALIDA.mkdir(exist_ok=True)

# montos con separador de miles (1,200,000) en vez de notación científica (1.2e6)
FORMATO_DINERO = StrMethodFormatter("{x:,.0f}")

# nombres legibles para los códigos que guarda el backend
TIPOS_CUENTA = {"AH": "Ahorro", "CC": "Corriente"}
TIPOS_MOVIMIENTO = {
    "deposit": "Depósito",
    "withdrawal": "Retiro",
    "transfer-out": "Transferencia enviada",
    "transfer-in": "Transferencia recibida",
    "interest": "Intereses",
    "card-payment": "Pago de tarjeta",
}


def obtener(endpoint):
    url = f"{BASE_URL}/{endpoint}"
    try:
        respuesta = requests.get(url, timeout=10)
        respuesta.raise_for_status()
        return pd.DataFrame(respuesta.json())
    except requests.RequestException as error:
        print(f"No fue posible consultar {url}: {error}")
        print("Verifica que Spring Boot esté ejecutándose y que MySQL esté conectado.")
        sys.exit(1)


def guardar_grafica(ax, nombre):
    ax.figure.tight_layout()
    ax.figure.savefig(SALIDA / nombre, dpi=150)
    plt.close(ax.figure)
    print(f"  Gráfica guardada: {nombre}")


def sin_datos(nombre):
    print(f"  No hay registros de {nombre}: se omiten sus gráficas.")


def graficar_cuentas(cuentas):
    print("\n--- Cuentas ---")
    if cuentas.empty:
        sin_datos("cuentas")
        return
    cuentas["tipo"] = cuentas["tipocuenta"].map(TIPOS_CUENTA)

    # 1. Cantidad de cuentas por tipo + porcentaje calculado con NumPy
    conteo = cuentas.groupby("tipo").size().sort_values(ascending=False)
    total = np.sum(conteo.to_numpy())
    porcentajes = conteo.to_numpy() / total * 100 if total else np.zeros(len(conteo))
    resumen = pd.DataFrame({
        "tipo": conteo.index,
        "cantidad": conteo.to_numpy(),
        "porcentaje": np.round(porcentajes, 1),
        "saldo_total": cuentas.groupby("tipo")["saldo"].sum().reindex(conteo.index).to_numpy(),
    })
    print(resumen.to_string(index=False))
    resumen.to_csv(SALIDA / "resumen_cuentas_por_tipo.csv", index=False, encoding="utf-8-sig")

    ax = conteo.plot(kind="pie", autopct="%1.1f%%", title="Cuentas por tipo", ylabel="")
    guardar_grafica(ax, "cuentas_por_tipo.png")

    # 2. Saldo total por tipo de cuenta
    saldo = cuentas.groupby("tipo")["saldo"].sum().sort_values(ascending=False)
    ax = saldo.plot(kind="bar", title="Saldo total por tipo de cuenta", xlabel="Tipo de cuenta", ylabel="Saldo", rot=0)
    ax.bar_label(ax.containers[0], fmt="{:,.0f}")
    ax.yaxis.set_major_formatter(FORMATO_DINERO)
    guardar_grafica(ax, "saldo_por_tipo.png")

    # 3. Distribución de saldos (cuántas cuentas tienen poco, medio o mucho dinero)
    ax = cuentas["saldo"].plot(kind="hist", bins=min(10, max(1, len(cuentas))), edgecolor="black",
                               title="Distribución de saldos")
    ax.set_xlabel("Saldo")
    ax.set_ylabel("Cantidad de cuentas")
    ax.yaxis.set_major_locator(MaxNLocator(integer=True))  # no hay "media cuenta"
    ax.xaxis.set_major_formatter(FORMATO_DINERO)
    guardar_grafica(ax, "distribucion_saldos.png")


def graficar_movimientos(movimientos):
    print("\n--- Movimientos ---")
    if movimientos.empty:
        sin_datos("movimientos")
        return
    movimientos["tipo"] = movimientos["tipomovimiento"].map(TIPOS_MOVIMIENTO).fillna(movimientos["tipomovimiento"])
    movimientos["fecha"] = pd.to_datetime(movimientos["fecha"])

    # 4. Cantidad de movimientos por tipo
    conteo = movimientos.groupby("tipo").size().sort_values(ascending=False)
    print(conteo.to_string())
    ax = conteo.plot(kind="bar", title="Movimientos por tipo", xlabel="Tipo", ylabel="Cantidad", rot=20)
    ax.bar_label(ax.containers[0])
    guardar_grafica(ax, "movimientos_por_tipo.png")

    # 5. Dinero movido por día
    por_dia = movimientos.groupby(movimientos["fecha"].dt.strftime("%Y-%m-%d"))["monto"].sum()
    por_dia.to_csv(SALIDA / "dinero_movido_por_dia.csv", header=["monto"], encoding="utf-8-sig")
    ax = por_dia.plot(kind="line", marker="o", title="Dinero movido por día", xlabel="Fecha", ylabel="Monto", rot=30)
    ax.yaxis.set_major_formatter(FORMATO_DINERO)
    ax.set_xticks(range(len(por_dia)), por_dia.index)  # una etiqueta por cada día
    ax.set_ylim(bottom=0)
    guardar_grafica(ax, "dinero_movido_por_dia.png")


def graficar_tarjetas(tarjetas):
    print("\n--- Tarjetas de crédito ---")
    if tarjetas.empty:
        sin_datos("tarjetas")
        return

    # 6. Uso del cupo: deuda vs límite de cada tarjeta
    uso = tarjetas.set_index("id")[["deudaactual", "limitecredito"]]
    uso.columns = ["Deuda actual", "Límite de crédito"]
    porcentaje_uso = np.round(uso["Deuda actual"].to_numpy() / uso["Límite de crédito"].to_numpy() * 100, 1)
    print(pd.DataFrame({"tarjeta": uso.index, "uso_%": porcentaje_uso}).to_string(index=False))
    ax = uso.plot(kind="bar", title="Uso del cupo de las tarjetas", xlabel="Tarjeta (id)", ylabel="Monto", rot=0)
    ax.yaxis.set_major_formatter(FORMATO_DINERO)
    guardar_grafica(ax, "uso_cupo_tarjetas.png")


def graficar_personas(personas):
    print("\n--- Usuarios ---")
    if personas.empty:
        sin_datos("usuarios")
        return

    # 7. Usuarios activos vs bloqueados
    conteo = personas["activo"].map({True: "Activo", False: "Bloqueado"}).value_counts()
    print(conteo.to_string())
    ax = conteo.plot(kind="bar", title="Usuarios activos vs bloqueados", xlabel="Estado", ylabel="Cantidad", rot=0)
    ax.bar_label(ax.containers[0])
    guardar_grafica(ax, "usuarios_por_estado.png")


def main():
    print("\n=== BANKING SYSTEM: INFORME GENERAL ===")
    cuentas = obtener("cuentas")
    movimientos = obtener("movimientos")
    tarjetas = obtener("tarjetas")
    personas = obtener("personas")

    print(f"\nTotal de usuarios: {len(personas)}")
    print(f"Total de cuentas: {len(cuentas)}")
    print(f"Total de movimientos: {len(movimientos)}")
    print(f"Total de tarjetas: {len(tarjetas)}")

    for nombre, datos in [("cuentas", cuentas), ("movimientos", movimientos),
                          ("tarjetas", tarjetas), ("usuarios", personas)]:
        datos.to_csv(SALIDA / f"{nombre}.csv", index=False, encoding="utf-8-sig")

    graficar_cuentas(cuentas)
    graficar_movimientos(movimientos)
    graficar_tarjetas(tarjetas)
    graficar_personas(personas)

    print(f"\nInformes y gráficos guardados en: {SALIDA}")
    print("Listo. Revisa los archivos CSV y PNG generados en la carpeta reportes.")


if __name__ == "__main__":
    main()
