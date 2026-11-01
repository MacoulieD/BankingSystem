"""Arma el informe general de BankingSystem en un solo PDF (una página por tema)
con Matplotlib (PdfPages). Cada página trae su gráfica y un texto que explica
lo que muestra; las cifras del texto se calculan con los datos de la BD.

Lo llama reporte_banking.py con los DataFrames que ya descargó de /api/reportes.
"""
from datetime import datetime
import textwrap
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
from matplotlib.ticker import FuncFormatter, MaxNLocator

# paleta categórica (orden fijo) y tinta del texto
AZUL, NARANJA, AGUAMARINA, AMARILLO, MAGENTA, VERDE, VIOLETA, ROJO = (
    "#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#008300", "#6250d6", "#e34948")
CATEGORICOS = [AZUL, NARANJA, AGUAMARINA, AMARILLO, MAGENTA, VERDE, VIOLETA, ROJO]
GRIS_CLARO = "#d9d8d4"
TINTA, TINTA_SUAVE, FONDO = "#0b0b0b", "#52514e", "#fcfcfb"

TAMANO_A4 = (11.69, 8.27)  # horizontal
NOMBRES_MOVIMIENTO = {
    "deposit": "Depósito", "withdrawal": "Retiro", "transfer-out": "Transferencia enviada",
    "transfer-in": "Transferencia recibida", "interest": "Intereses", "card-payment": "Pago de tarjeta",
}
ENTRADAS = ["deposit", "transfer-in", "interest"]
SALIDAS = ["withdrawal", "transfer-out", "card-payment"]
PLAZOS = [1, 3, 6, 12, 24, 36]

plt.rcParams.update({
    "font.family": "DejaVu Sans", "font.size": 10, "text.color": TINTA,
    "axes.edgecolor": GRIS_CLARO, "axes.labelcolor": TINTA_SUAVE, "axes.titlesize": 12,
    "axes.titleweight": "bold", "axes.titlelocation": "left", "axes.spines.top": False,
    "axes.spines.right": False, "xtick.color": TINTA_SUAVE, "ytick.color": TINTA_SUAVE,
    "axes.grid": True, "grid.color": "#ecebe7", "grid.linewidth": 0.8, "axes.axisbelow": True,
    "figure.facecolor": FONDO, "axes.facecolor": FONDO, "legend.frameon": False,
    "text.parse_math": False,  # los "$" de los pesos son texto, no fórmulas
})


# ---------- formatos ----------
def pesos(valor):
    """1500000 -> '$ 1.500.000' (formato colombiano)."""
    return "$ " + f"{valor:,.0f}".replace(",", ".")


def millones(valor, _=None):
    """Eje de dinero: 2500000 -> '$ 2,5 M'."""
    if abs(valor) >= 1_000_000:
        return "$ " + f"{valor / 1_000_000:,.1f}".replace(".", ",").replace(",0", "") + " M"
    return "$ " + f"{valor / 1_000:,.0f}" + " mil" if valor else "$ 0"


def decimal(valor):
    """2.9 -> '2,9' (coma decimal)."""
    return f"{valor:.1f}".replace(".", ",")


def porcentaje(parte, total):
    return f"{(parte / total * 100 if total else 0):.0f} %".replace(".", ",")


def ultimos4(numero):
    return "****" + str(numero)[-4:]


# ---------- página ----------
class Informe:
    def __init__(self, ruta):
        self.pdf = PdfPages(ruta)
        self.numero = 0

    def pagina(self, titulo, subtitulo):
        self.numero += 1
        fig = plt.figure(figsize=TAMANO_A4)
        fig.text(0.05, 0.94, titulo, fontsize=20, weight="bold")
        fig.text(0.05, 0.905, subtitulo, fontsize=11, color=TINTA_SUAVE)
        fig.add_artist(plt.Line2D([0.05, 0.95], [0.885, 0.885], color=GRIS_CLARO, linewidth=1))
        fig.text(0.05, 0.025, "MiPlata · Informe general de BankingSystem", fontsize=8, color=TINTA_SUAVE)
        fig.text(0.95, 0.025, f"Página {self.numero}", fontsize=8, color=TINTA_SUAVE, ha="right")
        return fig

    def zona(self, fig, columnas=1, anchos=None):
        """Ejes para las gráficas, entre el título y el texto descriptivo."""
        grid = fig.add_gridspec(1, columnas, left=0.07, right=0.95, top=0.83, bottom=0.33,
                                wspace=0.3, width_ratios=anchos)
        return [fig.add_subplot(grid[0, i]) for i in range(columnas)]

    def descripcion(self, fig, parrafos):
        """Texto explicativo de la página (abajo), con viñetas."""
        lineas = []
        for parrafo in parrafos:
            envuelto = textwrap.wrap(parrafo, 132)
            lineas.append("•  " + envuelto[0])
            lineas += ["    " + linea for linea in envuelto[1:]]
        fig.add_artist(plt.Rectangle((0.05, 0.06), 0.9, 0.2, facecolor="#f3f2ee", edgecolor="none",
                                     transform=fig.transFigure))
        fig.text(0.07, 0.235, "¿Qué muestra esta página?", fontsize=10, weight="bold", va="top")
        fig.text(0.07, 0.2, "\n".join(lineas), fontsize=9.5, va="top", linespacing=1.6)

    def guardar(self, fig):
        self.pdf.savefig(fig)
        plt.close(fig)

    def cerrar(self):
        info = self.pdf.infodict()
        info["Title"] = "Informe general de BankingSystem"
        info["Author"] = "BankingSystem - python/reporte_banking.py"
        self.pdf.close()


# ---------- ayudas de gráficas ----------
def torta(ax, valores, etiquetas, colores, titulo, leyenda=None, hueco=0.45):
    """Dona con el porcentaje dentro; la leyenda lleva la etiqueta y su valor
    (la cantidad, o el texto que se pase en leyenda)."""
    total = sum(valores)
    sectores, _, textos = ax.pie(
        valores, colors=colores, startangle=90, counterclock=False, autopct=lambda p: f"{p:.0f} %" if p >= 4 else "",
        pctdistance=1 - hueco / 2, wedgeprops={"width": hueco, "edgecolor": FONDO, "linewidth": 2},
        textprops={"color": "white", "fontsize": 10, "weight": "bold"})
    ax.set_title(titulo)
    if leyenda is None:
        leyenda = [f"{e} ({int(v)})" for e, v in zip(etiquetas, valores)]
    ax.legend(sectores, leyenda,
              loc="upper center", bbox_to_anchor=(0.5, 0.02), fontsize=9,
              ncol=(1 if len(leyenda) <= 3 else 2) if max(map(len, leyenda)) > 22 else 3 if len(leyenda) > 4 else 2)
    ax.axis("equal")
    return total


def barras_con_valor(ax, contenedor, formato=pesos):
    ax.bar_label(contenedor, labels=[formato(v) for v in contenedor.datavalues], padding=3,
                 fontsize=8.5, color=TINTA)


def eje_dinero(ax, eje="y"):
    eje = ax.yaxis if eje == "y" else ax.xaxis
    eje.set_major_formatter(FuncFormatter(millones))
    eje.set_major_locator(MaxNLocator(6))


def sin_datos(ax, mensaje):
    ax.axis("off")
    ax.text(0.5, 0.5, mensaje, ha="center", va="center", color=TINTA_SUAVE, fontsize=12)


# ---------- páginas ----------
def portada(informe, cuentas, movimientos, tarjetas, personas, compras):
    fig = informe.pagina("Informe general de BankingSystem",
                         f"Datos de la base dbbankingsystem · generado el {datetime.now():%d/%m/%Y %H:%M}")
    intereses = (compras["total"] - compras["capital"]).sum() if not compras.empty else 0
    indicadores = [
        ("Usuarios", f"{len(personas)}", f"{int((~personas['activo']).sum())} bloqueado(s)" if not personas.empty else ""),
        ("Cuentas", f"{len(cuentas)}", f"{(cuentas['tipocuenta'] == 'AH').sum()} ahorro · {(cuentas['tipocuenta'] == 'CC').sum()} corriente"),
        ("Dinero en cuentas", pesos(cuentas["saldo"].sum()), "suma de todos los saldos"),
        ("Movimientos", f"{len(movimientos)}", pesos(movimientos["monto"].sum()) + " movidos" if not movimientos.empty else ""),
        ("Tarjetas de crédito", f"{len(tarjetas)}", pesos(tarjetas["limitecredito"].sum()) + " de cupo" if not tarjetas.empty else ""),
        ("Deuda en tarjetas", pesos(tarjetas["deudaactual"].sum()) if not tarjetas.empty else "$ 0", "lo que deben los clientes"),
        ("Compras con tarjeta", f"{len(compras)}", pesos(compras["capital"].sum()) + " en compras" if not compras.empty else ""),
        ("Intereses de las compras", pesos(intereses), "1,8 % mensual en 3 a 36 cuotas"),
    ]
    for i, (nombre, valor, detalle) in enumerate(indicadores):
        fila, columna = divmod(i, 4)
        x, y = 0.05 + columna * 0.228, 0.62 - fila * 0.25
        fig.add_artist(plt.Rectangle((x, y), 0.21, 0.2, facecolor="white", edgecolor=GRIS_CLARO,
                                     transform=fig.transFigure))
        fig.add_artist(plt.Rectangle((x, y + 0.193), 0.21, 0.007, facecolor=CATEGORICOS[i], edgecolor="none",
                                     transform=fig.transFigure))
        fig.text(x + 0.015, y + 0.155, nombre, fontsize=10, color=TINTA_SUAVE)
        fig.text(x + 0.015, y + 0.075, valor, fontsize=19 if len(valor) < 14 else 15, weight="bold")
        fig.text(x + 0.015, y + 0.03, detalle, fontsize=8.5, color=TINTA_SUAVE)
    desde, hasta = (movimientos["fecha"].min(), movimientos["fecha"].max()) if not movimientos.empty else (None, None)
    periodo = f"entre el {desde:%d/%m/%Y} y el {hasta:%d/%m/%Y}" if desde is not None else "sin movimientos aún"
    fig.text(0.05, 0.27, "Contenido", fontsize=11, weight="bold")
    fig.text(0.05, 0.235, textwrap.fill(
        "1. Cuentas: tipos, saldos, ranking y distribución.  2. Sobregiro de las cuentas corrientes.  "
        "3. Movimientos: tipos, entradas vs salidas, destinos de las transferencias y dinero movido por día.  "
        "4. Tarjetas de crédito: uso del cupo, compras por plazo y costo de los intereses.  "
        "5. Usuarios: estado e intentos fallidos.  6. Conclusiones.  "
        f"Los movimientos analizados ocurrieron {periodo}. Los números de cuenta se muestran ocultos (****1234) "
        "y el informe no incluye nombres, documentos ni datos de las tarjetas.", 150),
        fontsize=9.5, color=TINTA_SUAVE, linespacing=1.6, va="top")
    informe.guardar(fig)


def pagina_cuentas(informe, cuentas):
    fig = informe.pagina("Cuentas por tipo", "¿Cuántas cuentas hay de cada tipo y cuánto dinero guarda cada una?")
    ax1, ax2 = informe.zona(fig, 2)
    resumen = cuentas.groupby("tipo").agg(cantidad=("saldo", "size"), saldo=("saldo", "sum"),
                                          promedio=("saldo", "mean")).reindex(["Ahorro", "Corriente"]).fillna(0)
    torta(ax1, resumen["cantidad"], resumen.index, [AZUL, NARANJA], "Cantidad de cuentas")
    barras = ax2.bar(resumen.index, resumen["saldo"], color=[AZUL, NARANJA], width=0.5)
    barras_con_valor(ax2, barras)
    ax2.set_title("Dinero total por tipo de cuenta")
    eje_dinero(ax2)
    ax2.grid(axis="x", visible=False)
    total = resumen["saldo"].sum()
    mayor = resumen["saldo"].idxmax()
    informe.descripcion(fig, [
        f"Hay {int(resumen['cantidad'].sum())} cuentas: {int(resumen.loc['Ahorro', 'cantidad'])} de ahorros "
        f"({porcentaje(resumen.loc['Ahorro', 'cantidad'], resumen['cantidad'].sum())}) y "
        f"{int(resumen.loc['Corriente', 'cantidad'])} corrientes.",
        f"Entre todas guardan {pesos(total)}. Las cuentas de {mayor.lower()} concentran el "
        f"{porcentaje(resumen.loc[mayor, 'saldo'], total)} del dinero.",
        f"Saldo promedio: ahorro {pesos(resumen.loc['Ahorro', 'promedio'])} y corriente "
        f"{pesos(resumen.loc['Corriente', 'promedio'])}. La cuenta de ahorros gana 0,5 % de interés; la corriente "
        "no gana interés pero tiene cupo de sobregiro del 20 % del saldo con que se abrió.",
    ])
    informe.guardar(fig)


def pagina_ranking(informe, cuentas):
    fig = informe.pagina("Ranking de cuentas por saldo", "Todas las cuentas ordenadas de mayor a menor saldo")
    ax, = informe.zona(fig)
    orden = cuentas.sort_values("saldo")
    colores = [AZUL if t == "Ahorro" else NARANJA for t in orden["tipo"]]
    barras = ax.barh(orden["numerocuenta"].map(ultimos4), orden["saldo"], color=colores, height=0.65)
    barras_con_valor(ax, barras)
    eje_dinero(ax, "x")
    ax.grid(axis="y", visible=False)
    ax.set_title("Saldo de cada cuenta")
    ax.legend(handles=[plt.Rectangle((0, 0), 1, 1, color=AZUL), plt.Rectangle((0, 0), 1, 1, color=NARANJA)],
              labels=["Ahorro", "Corriente"], loc="lower right")
    ax.set_xlim(right=orden["saldo"].max() * 1.18)
    ax.tick_params(axis="y", labelsize=8.5)
    top3 = cuentas["saldo"].nlargest(3).sum()
    informe.descripcion(fig, [
        f"La cuenta con más dinero es {ultimos4(orden.iloc[-1]['numerocuenta'])} ({orden.iloc[-1]['tipo'].lower()}) con "
        f"{pesos(orden.iloc[-1]['saldo'])}; la de menos es {ultimos4(orden.iloc[0]['numerocuenta'])} con {pesos(orden.iloc[0]['saldo'])}.",
        f"El saldo promedio es {pesos(cuentas['saldo'].mean())} y la mediana {pesos(cuentas['saldo'].median())}: "
        + ("el promedio es mayor porque unas pocas cuentas tienen saldos altos." if cuentas['saldo'].mean() > cuentas['saldo'].median()
           else "los saldos están repartidos de forma pareja."),
        f"Las 3 cuentas más grandes tienen el {porcentaje(top3, cuentas['saldo'].sum())} de todo el dinero del banco.",
    ])
    informe.guardar(fig)


def pagina_distribucion(informe, cuentas):
    fig = informe.pagina("Distribución de los saldos", "¿En qué rango de saldo están las cuentas?")
    ax1, ax2 = informe.zona(fig, 2, anchos=[1.6, 1])
    rangos = [0, 1_000_000, 5_000_000, 10_000_000, 20_000_000, np.inf]
    nombres = ["Menos de $ 1 M", "$ 1 M a $ 5 M", "$ 5 M a $ 10 M", "$ 10 M a $ 20 M", "Más de $ 20 M"]
    cuentas["rango"] = pd.cut(cuentas["saldo"].clip(lower=0), rangos, labels=nombres, right=False)
    tabla = pd.crosstab(cuentas["rango"], cuentas["tipo"]).reindex(nombres, fill_value=0)
    tabla = tabla.reindex(columns=["Ahorro", "Corriente"], fill_value=0)
    base = np.zeros(len(tabla))
    for tipo, color in [("Ahorro", AZUL), ("Corriente", NARANJA)]:
        ax1.bar(tabla.index, tabla[tipo], bottom=base, color=color, width=0.6, label=tipo, edgecolor=FONDO, linewidth=1.5)
        base += tabla[tipo].to_numpy()
    for x, total in enumerate(base):
        if total:
            ax1.text(x, total + 0.05, f"{int(total)}", ha="center", va="bottom", fontsize=9)
    ax1.yaxis.set_major_locator(MaxNLocator(integer=True))
    ax1.set_ylabel("Cantidad de cuentas")
    ax1.set_title("Cuentas por rango de saldo")
    ax1.grid(axis="x", visible=False)
    ax1.tick_params(axis="x", labelsize=8.5)
    ax1.legend(loc="upper right")
    ax2.boxplot([cuentas.loc[cuentas["tipo"] == t, "saldo"] for t in ["Ahorro", "Corriente"]], tick_labels=["Ahorro", "Corriente"],
                widths=0.5, patch_artist=True, boxprops={"facecolor": "#dbe8f8", "edgecolor": AZUL},
                medianprops={"color": NARANJA, "linewidth": 2}, whiskerprops={"color": TINTA_SUAVE},
                capprops={"color": TINTA_SUAVE}, flierprops={"markeredgecolor": TINTA_SUAVE})
    eje_dinero(ax2)
    ax2.set_title("Dispersión de saldos (cajas)")
    ax2.grid(axis="x", visible=False)
    mas_comun = tabla.sum(axis=1).idxmax()
    informe.descripcion(fig, [
        f"El rango más común es «{mas_comun}», con {int(tabla.sum(axis=1).max())} cuentas. Las barras separan "
        "las cuentas de ahorro (azul) y las corrientes (naranja).",
        "La gráfica de cajas (derecha) resume cada tipo: la caja va del 25 % al 75 % de los saldos, la línea naranja "
        "es la mediana y los bigotes el mínimo y el máximo; los puntos sueltos son saldos muy diferentes al resto.",
        f"Mediana de ahorro: {pesos(cuentas.loc[cuentas['tipo'] == 'Ahorro', 'saldo'].median())} · "
        f"mediana corriente: {pesos(cuentas.loc[cuentas['tipo'] == 'Corriente', 'saldo'].median())}.",
    ])
    informe.guardar(fig)


def pagina_sobregiro(informe, cuentas):
    fig = informe.pagina("Sobregiro de las cuentas corrientes",
                         "Saldo de cada cuenta corriente frente a su cupo de sobregiro (20 % del saldo inicial)")
    ax, = informe.zona(fig)
    corrientes = cuentas[cuentas["tipo"] == "Corriente"].sort_values("saldo", ascending=False)
    if corrientes.empty:
        sin_datos(ax, "No hay cuentas corrientes")
        informe.guardar(fig)
        return
    x = np.arange(len(corrientes))
    ancho = 0.38
    b1 = ax.bar(x - ancho / 2, corrientes["saldo"], ancho, color=AZUL, label="Saldo actual")
    b2 = ax.bar(x + ancho / 2, corrientes["sobregiro"], ancho, color=AGUAMARINA, label="Cupo de sobregiro")
    barras_con_valor(ax, b1)
    barras_con_valor(ax, b2)
    ax.axhline(0, color=TINTA_SUAVE, linewidth=0.8)
    ax.set_xticks(x, corrientes["numerocuenta"].map(ultimos4))
    eje_dinero(ax)
    ax.grid(axis="x", visible=False)
    ax.legend(loc="upper right")
    ax.set_title("Saldo y cupo de sobregiro por cuenta corriente")
    usando = corrientes[corrientes["saldo"] < 0]
    disponible = corrientes["saldo"].clip(lower=0) + corrientes["sobregiro"] + corrientes["saldo"].clip(upper=0)
    informe.descripcion(fig, [
        f"Hay {len(corrientes)} cuentas corrientes con un cupo de sobregiro total de {pesos(corrientes['sobregiro'].sum())}. "
        "El sobregiro deja retirar o transferir más dinero del que hay en la cuenta, hasta ese cupo.",
        (f"{len(usando)} cuenta(s) están usando el sobregiro ahora (saldo negativo), por {pesos(-usando['saldo'].sum())}."
         if len(usando) else "Ninguna cuenta está usando el sobregiro en este momento: todos los saldos son positivos."),
        f"Dinero que los clientes pueden gastar sumando saldo y sobregiro: {pesos(disponible.sum())}.",
    ])
    informe.guardar(fig)


def pagina_movimientos(informe, movimientos):
    fig = informe.pagina("Movimientos por tipo", "¿Qué operaciones hacen los clientes y cuánto dinero mueve cada una?")
    ax1, ax2 = informe.zona(fig, 2)
    resumen = movimientos.groupby("tipo").agg(cantidad=("monto", "size"), dinero=("monto", "sum")).sort_values("dinero")
    colores = {t: CATEGORICOS[i] for i, t in enumerate(resumen.sort_values("cantidad", ascending=False).index)}
    por_cantidad = resumen.sort_values("cantidad", ascending=False)
    torta(ax1, por_cantidad["cantidad"], por_cantidad.index, [colores[t] for t in por_cantidad.index], "Cantidad de movimientos")
    barras = ax2.barh(resumen.index, resumen["dinero"], color=[colores[t] for t in resumen.index], height=0.6)
    barras_con_valor(ax2, barras)
    eje_dinero(ax2, "x")
    ax2.set_xlim(right=resumen["dinero"].max() * 1.35)
    ax2.grid(axis="y", visible=False)
    ax2.set_title("Dinero movido por tipo")
    mas_frecuente, mas_dinero = por_cantidad.index[0], resumen.index[-1]
    promedio = movimientos.groupby("tipo")["monto"].mean()
    informe.descripcion(fig, [
        f"Se registraron {len(movimientos)} movimientos por {pesos(movimientos['monto'].sum())}. El más frecuente es "
        f"«{mas_frecuente}» ({int(por_cantidad.iloc[0]['cantidad'])} veces) y el que más dinero mueve es «{mas_dinero}» "
        f"({pesos(resumen.iloc[-1]['dinero'])}).",
        "Valor promedio por operación: " + " · ".join(f"{t.lower()} {pesos(v)}" for t, v in promedio.sort_values(ascending=False).items()) + ".",
        "Cada transferencia entre clientes MiPlata genera dos movimientos: la enviada (en quien envía) y la recibida (en quien recibe).",
    ])
    informe.guardar(fig)


def pagina_entradas_salidas(informe, movimientos):
    fig = informe.pagina("Dinero que entra vs dinero que sale", "Entradas (depósitos y transferencias recibidas) frente a salidas por día")
    ax, = informe.zona(fig)
    movimientos["sentido"] = np.where(movimientos["tipomovimiento"].isin(ENTRADAS), "Entradas", "Salidas")
    tabla = movimientos.pivot_table(index="dia", columns="sentido", values="monto", aggfunc="sum", fill_value=0)
    tabla = tabla.reindex(columns=["Entradas", "Salidas"], fill_value=0)
    x = np.arange(len(tabla))
    ancho = 0.4
    ax.bar(x - ancho / 2, tabla["Entradas"], ancho, color=AGUAMARINA, label="Entradas")
    ax.bar(x + ancho / 2, tabla["Salidas"], ancho, color=NARANJA, label="Salidas")
    ax.set_xticks(x, [d.strftime("%d/%m") for d in tabla.index])
    eje_dinero(ax)
    ax.grid(axis="x", visible=False)
    ax.legend(loc="upper left")
    ax.set_title("Entradas y salidas de dinero por día")
    entradas, salidas = tabla["Entradas"].sum(), tabla["Salidas"].sum()
    neto = tabla["Entradas"] - tabla["Salidas"]
    informe.descripcion(fig, [
        f"En total entraron {pesos(entradas)} y salieron {pesos(salidas)}: "
        + (f"salió {pesos(salidas - entradas)} más de lo que entró." if salidas > entradas else f"entró {pesos(entradas - salidas)} más de lo que salió."),
        f"El día con más salidas fue el {tabla['Salidas'].idxmax():%d/%m} ({pesos(tabla['Salidas'].max())}) y el de más entradas "
        f"el {tabla['Entradas'].idxmax():%d/%m} ({pesos(tabla['Entradas'].max())}).",
        f"Días con saldo neto positivo: {(neto > 0).sum()} de {len(neto)}. Las salidas incluyen retiros, transferencias enviadas y pagos de tarjeta.",
    ])
    informe.guardar(fig)


def pagina_destinos(informe, movimientos):
    fig = informe.pagina("¿A dónde se transfiere el dinero?", "Transferencias enviadas según el destino")
    ax1, ax2 = informe.zona(fig, 2)
    enviadas = movimientos[movimientos["tipomovimiento"] == "transfer-out"].copy()
    if enviadas.empty:
        sin_datos(ax1, "No hay transferencias")
        ax2.axis("off")
        informe.guardar(fig)
        return

    def grupo(destino):
        if destino in ("Nequi", "Daviplata"):
            return "Billeteras (Nequi, Daviplata)"
        if destino in ("Entre mis cuentas", "Otro usuario MiPlata"):
            return "Dentro de MiPlata"
        return "Otros bancos"
    enviadas["grupo"] = enviadas["destino"].map(grupo)
    por_grupo = enviadas.groupby("grupo")["monto"].agg(["size", "sum"]).sort_values("sum", ascending=False)
    torta(ax1, por_grupo["sum"], por_grupo.index, CATEGORICOS[:len(por_grupo)], "Dinero enviado por tipo de destino",
          leyenda=[f"{g}: {pesos(v)} · {n} envíos" for g, v, n in zip(por_grupo.index, por_grupo["sum"], por_grupo["size"])])
    por_destino = enviadas.groupby("destino")["monto"].sum().sort_values()
    barras = ax2.barh(por_destino.index, por_destino, color=AZUL, height=0.6)
    barras_con_valor(ax2, barras)
    eje_dinero(ax2, "x")
    ax2.set_xlim(right=por_destino.max() * 1.4)
    ax2.grid(axis="y", visible=False)
    ax2.set_title("Dinero enviado por destino")
    principal = por_grupo.index[0]
    informe.descripcion(fig, [
        f"Se hicieron {len(enviadas)} transferencias por {pesos(enviadas['monto'].sum())}. La mayor parte del dinero "
        f"({porcentaje(por_grupo.iloc[0]['sum'], por_grupo['sum'].sum())}) va a «{principal}».",
        f"El destino individual que más dinero recibe es «{por_destino.index[-1]}» ({pesos(por_destino.iloc[-1])}).",
        "«Dentro de MiPlata» une las transferencias entre cuentas propias y a otros clientes del banco: ese dinero no sale del banco.",
    ])
    informe.guardar(fig)


def pagina_por_dia(informe, movimientos):
    fig = informe.pagina("Actividad por día", "Dinero movido y cantidad de operaciones cada día")
    ax1, ax2 = informe.zona(fig, 2, anchos=[1.5, 1])
    por_dia = movimientos.groupby("dia")["monto"].agg(["sum", "size"])
    etiquetas = [d.strftime("%d/%m") for d in por_dia.index]
    ax1.plot(etiquetas, por_dia["sum"], color=AZUL, linewidth=2, marker="o", markersize=7,
             markeredgecolor=FONDO, markeredgewidth=2)
    ax1.fill_between(etiquetas, por_dia["sum"], color=AZUL, alpha=0.08)
    pico = por_dia["sum"].idxmax()
    ax1.annotate(f"Pico: {pesos(por_dia['sum'].max())}", (etiquetas[list(por_dia.index).index(pico)], por_dia["sum"].max()),
                 xytext=(0, 10), textcoords="offset points", ha="center", fontsize=9)
    eje_dinero(ax1)
    ax1.set_ylim(bottom=0, top=por_dia["sum"].max() * 1.2)
    ax1.set_title("Dinero movido por día")
    ax1.tick_params(axis="x", labelsize=8.5, rotation=45)
    barras = ax2.bar(etiquetas, por_dia["size"], color=VIOLETA, width=0.6)
    ax2.bar_label(barras, fontsize=8.5)
    ax2.yaxis.set_major_locator(MaxNLocator(integer=True))
    ax2.set_title("Operaciones por día")
    ax2.grid(axis="x", visible=False)
    ax2.tick_params(axis="x", labelsize=8.5, rotation=45)
    informe.descripcion(fig, [
        f"Hubo movimientos en {len(por_dia)} días diferentes, con un promedio de {pesos(por_dia['sum'].mean())} y "
        f"{decimal(por_dia['size'].mean())} operaciones por día.",
        f"El día de mayor movimiento fue el {pico:%d/%m/%Y}, con {pesos(por_dia['sum'].max())} en {int(por_dia.loc[pico, 'size'])} operaciones.",
        "Las dos gráficas usan escalas distintas a propósito: la de la izquierda mide dinero y la de la derecha cantidad de operaciones.",
    ])
    informe.guardar(fig)


def pagina_tarjetas(informe, tarjetas):
    fig = informe.pagina("Tarjetas de crédito: uso del cupo", "¿Cuánto del cupo aprobado están usando los clientes?")
    ax1, ax2 = informe.zona(fig, 2)
    if tarjetas.empty:
        sin_datos(ax1, "No hay tarjetas")
        ax2.axis("off")
        informe.guardar(fig)
        return
    usado, cupo = tarjetas["deudaactual"].sum(), tarjetas["limitecredito"].sum()
    torta(ax1, [usado, cupo - usado], ["Cupo usado", "Cupo disponible"], [NARANJA, GRIS_CLARO], "Cupo total de todas las tarjetas",
          leyenda=[f"Cupo usado: {pesos(usado)}", f"Cupo disponible: {pesos(cupo - usado)}"])
    tarjetas = tarjetas.assign(uso=tarjetas["deudaactual"] / tarjetas["limitecredito"] * 100).sort_values("uso")
    colores = [ROJO if u >= 70 else AMARILLO if u >= 40 else AGUAMARINA for u in tarjetas["uso"]]
    barras = ax2.barh([f"Tarjeta {i}" for i in tarjetas["id"]], tarjetas["uso"], color=colores, height=0.6)
    ax2.bar_label(barras, labels=[f"{u:.0f} %  ({pesos(d)} de {pesos(l)})" for u, d, l in
                                  zip(tarjetas["uso"], tarjetas["deudaactual"], tarjetas["limitecredito"])],
                  padding=3, fontsize=8)
    ax2.set_xlim(0, 160)
    ax2.set_xticks([0, 25, 50, 75, 100])
    ax2.xaxis.set_major_formatter(FuncFormatter(lambda v, _: f"{v:.0f} %"))
    ax2.grid(axis="y", visible=False)
    ax2.set_title("Porcentaje del cupo usado por tarjeta")
    ax2.legend(handles=[plt.Rectangle((0, 0), 1, 1, color=c) for c in [AGUAMARINA, AMARILLO, ROJO]],
               labels=["Menos del 40 %", "40 % a 69 %", "70 % o más"], loc="lower right", fontsize=8)
    altas = (tarjetas["uso"] >= 70).sum()
    informe.descripcion(fig, [
        f"Hay {len(tarjetas)} tarjetas con un cupo total de {pesos(cupo)}; los clientes deben {pesos(usado)}, "
        f"el {porcentaje(usado, cupo)} del cupo.",
        f"La tarjeta más usada lleva el {tarjetas['uso'].max():.0f} % de su cupo y la menos usada el {tarjetas['uso'].min():.0f} %. "
        + (f"{altas} tarjeta(s) superan el 70 %: conviene vigilar su capacidad de pago." if altas else "Ninguna supera el 70 % de su cupo."),
        "Colores de la derecha: verde = uso bajo, amarillo = uso medio, rojo = uso alto.",
    ])
    informe.guardar(fig)


def pagina_compras(informe, compras):
    fig = informe.pagina("Compras con tarjeta por número de cuotas", "¿A cuántas cuotas compran los clientes y cuánto pagan de intereses?")
    ax1, ax2 = informe.zona(fig, 2)
    if compras.empty:
        sin_datos(ax1, "No hay compras con tarjeta")
        ax2.axis("off")
        informe.guardar(fig)
        return
    compras = compras.assign(intereses=compras["total"] - compras["capital"])
    plazo = compras.groupby("cuotas").agg(compras=("id", "size"), capital=("capital", "sum"),
                                          intereses=("intereses", "sum")).reindex(PLAZOS, fill_value=0)
    nombres = [f"{c} cuota" if c == 1 else f"{c} cuotas" for c in plazo.index]
    con_compras = plazo["compras"] > 0
    torta(ax1, plazo.loc[con_compras, "compras"], [n for n, s in zip(nombres, con_compras) if s],
          [c for c, s in zip(CATEGORICOS, con_compras) if s], "Cantidad de compras por plazo")
    ax2.bar(nombres, plazo["capital"], color=AZUL, width=0.6, label="Capital (valor de la compra)",
            edgecolor=FONDO, linewidth=1.5)
    ax2.bar(nombres, plazo["intereses"], bottom=plazo["capital"], color=NARANJA, width=0.6, label="Intereses",
            edgecolor=FONDO, linewidth=1.5)
    for x, (c, i) in enumerate(zip(plazo["capital"], plazo["intereses"])):
        if c:
            ax2.text(x, c + i, millones(c + i), ha="center", va="bottom", fontsize=8.5)
    eje_dinero(ax2)
    ax2.grid(axis="x", visible=False)
    ax2.tick_params(axis="x", labelsize=8.5)
    ax2.legend(loc="upper left", fontsize=8.5)
    ax2.set_title("Total a pagar: capital + intereses")
    favorito = plazo["compras"].idxmax()
    informe.descripcion(fig, [
        f"Se hicieron {len(compras)} compras por {pesos(compras['capital'].sum())}. El plazo más usado es "
        f"{favorito} cuota{'s' if favorito > 1 else ''} ({int(plazo['compras'].max())} compras).",
        f"Con intereses, los clientes terminarán pagando {pesos(compras['total'].sum())}: {pesos(compras['intereses'].sum())} "
        "más que el valor de las compras.",
        "Comprar a 1 cuota no tiene interés. De 3 a 36 cuotas se cobra 1,8 % mensual (amortización francesa): a más cuotas, "
        "más intereses se suman al total (barra naranja).",
    ])
    informe.guardar(fig)


def pagina_costo_credito(informe, compras):
    fig = informe.pagina("¿Cuánto cuesta financiar una compra?", "Intereses como porcentaje del valor de la compra, según el plazo")
    ax1, ax2 = informe.zona(fig, 2)
    # simulación: compra de $ 1.000.000 con la misma fórmula del backend (STarjetaCredito)
    capital, tasa = 1_000_000, 0.018
    cuotas_mes = [capital if n == 1 else np.ceil(capital * tasa / (1 - (1 + tasa) ** -n)) for n in PLAZOS]
    extra = [(c * n - capital) / capital * 100 for c, n in zip(cuotas_mes, PLAZOS)]
    nombres = [f"{n}" for n in PLAZOS]
    colores = [GRIS_CLARO if e == 0 else NARANJA for e in extra]
    barras = ax1.bar(nombres, extra, color=colores, width=0.6)
    ax1.bar_label(barras, labels=[f"{e:.1f} %".replace(".", ",") for e in extra], padding=3, fontsize=9)
    ax1.yaxis.set_major_formatter(FuncFormatter(lambda v, _: f"{v:.0f} %"))
    ax1.set_xlabel("Número de cuotas")
    ax1.grid(axis="x", visible=False)
    ax1.set_title("Intereses extra sobre el valor de la compra")
    barras = ax2.bar(nombres, cuotas_mes, color=AZUL, width=0.6)
    barras_con_valor(ax2, barras)
    eje_dinero(ax2)
    ax2.set_xlabel("Número de cuotas")
    ax2.grid(axis="x", visible=False)
    ax2.set_title("Cuota mensual de una compra de $ 1.000.000")
    reales = ""
    if not compras.empty:
        con_interes = compras[compras["cuotas"] > 1]
        if not con_interes.empty:
            reales = (f"En las compras reales a cuotas, los intereses suman el "
                      f"{porcentaje((con_interes['total'] - con_interes['capital']).sum(), con_interes['capital'].sum())} de su valor.")
    informe.descripcion(fig, [
        f"Ejemplo con una compra de {pesos(capital)}: a 3 cuotas se pagan {pesos(cuotas_mes[1] * 3)} "
        f"({decimal(extra[1])} % más) y a 36 cuotas {pesos(cuotas_mes[-1] * 36)} ({decimal(extra[-1])} % más).",
        "La cuota mensual baja cuando se alarga el plazo, pero el total pagado sube: es el costo de financiar la compra a más tiempo.",
        reales or "Los valores salen de la misma fórmula que usa el backend para calcular las cuotas.",
    ])
    informe.guardar(fig)


def pagina_usuarios(informe, personas):
    fig = informe.pagina("Usuarios", "Estado de los usuarios e intentos fallidos de inicio de sesión")
    ax1, ax2 = informe.zona(fig, 2)
    activos, bloqueados = int(personas["activo"].sum()), int((~personas["activo"]).sum())
    torta(ax1, [activos, bloqueados] if bloqueados else [activos], ["Activos", "Bloqueados"][: 2 if bloqueados else 1],
          [AZUL, ROJO], "Estado de los usuarios")
    intentos = personas["failedAttempts"].value_counts().reindex([0, 1, 2, 3], fill_value=0)
    barras = ax2.bar(["0", "1", "2", "3 (bloqueo)"], intentos, color=[AZUL, AMARILLO, NARANJA, ROJO], width=0.6)
    ax2.bar_label(barras, fontsize=9)
    ax2.yaxis.set_major_locator(MaxNLocator(integer=True))
    ax2.set_xlabel("Intentos fallidos seguidos")
    ax2.set_ylabel("Usuarios")
    ax2.grid(axis="x", visible=False)
    ax2.set_title("Usuarios por intentos fallidos")
    informe.descripcion(fig, [
        f"Hay {len(personas)} usuarios registrados: {activos} activos y {bloqueados} bloqueado(s) "
        f"({porcentaje(bloqueados, len(personas))}).",
        "Un usuario se bloquea al fallar la contraseña 3 veces seguidas; al entrar bien el contador vuelve a 0.",
        f"{int(intentos[[1, 2]].sum())} usuario(s) tienen intentos fallidos sin llegar al bloqueo: están en riesgo de bloquearse.",
    ])
    informe.guardar(fig)


def pagina_conclusiones(informe, cuentas, movimientos, tarjetas, personas, compras):
    fig = informe.pagina("Conclusiones", "Lo más importante del informe")
    total = cuentas["saldo"].sum()
    ahorro = cuentas.loc[cuentas["tipo"] == "Ahorro", "saldo"].sum()
    entradas = movimientos.loc[movimientos["tipomovimiento"].isin(ENTRADAS), "monto"].sum()
    salidas = movimientos.loc[movimientos["tipomovimiento"].isin(SALIDAS), "monto"].sum()
    puntos = [
        ("Cuentas", f"El banco administra {pesos(total)} en {len(cuentas)} cuentas. Las de ahorro guardan el "
                    f"{porcentaje(ahorro, total)} del dinero, así que son el producto principal."),
        ("Movimientos", f"Entraron {pesos(entradas)} y salieron {pesos(salidas)}. "
                        + ("Salió más dinero del que entró: los clientes están usando sus ahorros." if salidas > entradas
                           else "Entró más dinero del que salió: los clientes están ahorrando.")),
    ]
    if not tarjetas.empty:
        puntos.append(("Tarjetas", f"Los clientes usan el {porcentaje(tarjetas['deudaactual'].sum(), tarjetas['limitecredito'].sum())} "
                                   f"del cupo total ({pesos(tarjetas['deudaactual'].sum())} de deuda)."))
    if not compras.empty:
        largas = compras[compras["cuotas"] >= 12]
        puntos.append(("Cuotas", f"{len(largas)} de {len(compras)} compras se difirieron a 12 cuotas o más; esas compras "
                                 f"generan {pesos((largas['total'] - largas['capital']).sum())} de los "
                                 f"{pesos((compras['total'] - compras['capital']).sum())} de intereses."))
    puntos.append(("Usuarios", f"{int((~personas['activo']).sum())} de {len(personas)} usuarios están bloqueados por intentos fallidos; "
                               "soporte debe revisarlos para desbloquearlos."))
    puntos.append(("Recomendación", "Registrar más operaciones en diferentes días permite ver tendencias más claras; "
                                    "este informe se puede volver a generar en cualquier momento con python reporte_banking.py."))
    y = 0.8
    for i, (tema, texto) in enumerate(puntos):
        fig.add_artist(plt.Rectangle((0.05, y - 0.015), 0.006, 0.07, facecolor=CATEGORICOS[i], edgecolor="none",
                                     transform=fig.transFigure))
        fig.text(0.07, y + 0.035, tema, fontsize=12, weight="bold")
        fig.text(0.07, y + 0.012, textwrap.fill(texto, 140), fontsize=10, color=TINTA_SUAVE, linespacing=1.5, va="top")
        y -= 0.12
    informe.guardar(fig)


def generar_informe(ruta, cuentas, movimientos, tarjetas, personas, compras):
    """Crea el PDF con todas las páginas. Recibe los DataFrames de /api/reportes."""
    cuentas = cuentas.copy()
    cuentas["tipo"] = cuentas["tipocuenta"].map({"AH": "Ahorro", "CC": "Corriente"})
    movimientos = movimientos.copy()
    movimientos["fecha"] = pd.to_datetime(movimientos["fecha"])
    movimientos["dia"] = movimientos["fecha"].dt.normalize()
    movimientos["tipo"] = movimientos["tipomovimiento"].map(NOMBRES_MOVIMIENTO).fillna(movimientos["tipomovimiento"])
    if "destino" not in movimientos:
        movimientos["destino"] = None

    informe = Informe(ruta)
    portada(informe, cuentas, movimientos, tarjetas, personas, compras)
    if not cuentas.empty:
        pagina_cuentas(informe, cuentas)
        pagina_ranking(informe, cuentas)
        pagina_distribucion(informe, cuentas)
        pagina_sobregiro(informe, cuentas)
    if not movimientos.empty:
        pagina_movimientos(informe, movimientos)
        pagina_entradas_salidas(informe, movimientos)
        pagina_destinos(informe, movimientos)
        pagina_por_dia(informe, movimientos)
    pagina_tarjetas(informe, tarjetas)
    pagina_compras(informe, compras)
    pagina_costo_credito(informe, compras)
    if not personas.empty:
        pagina_usuarios(informe, personas)
    if not cuentas.empty and not movimientos.empty and not personas.empty:
        pagina_conclusiones(informe, cuentas, movimientos, tarjetas, personas, compras)
    informe.cerrar()
    return informe.numero
