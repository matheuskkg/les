#!/usr/bin/env python3
"""Gera mer.drawio (páginas Conceitual e Lógico) a partir de conceitual.spec.json e mer.spec.json.

Uso: python3 docs/modelo/gerar_mer.py

O layout é fixo (coordenadas neste arquivo). Antes de gravar, o script confere o conteúdo, sobreposições e
linhas que passam por cima de caixas; se algo falhar, sai com código 1 sem gravar.
"""

from __future__ import annotations

import json
import math
import sys
import unicodedata
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path
from xml.sax.saxutils import quoteattr

BASE = Path(__file__).resolve().parent
SAIDA = BASE / "mer.drawio"

# (cabeçalho/entidade, borda, fundo do grupo) por módulo
PALETA = {
    "acesso": ("#dae8fc", "#6c8ebf", "#f4f8fd"),
    "clientes": ("#d5e8d4", "#82b366", "#f3f9f2"),
    "catalogo": ("#fff2cc", "#d6b656", "#fffbee"),
    "estoque": ("#ffe6cc", "#d79b00", "#fff8f0"),
    "vendas": ("#f8cecc", "#b85450", "#fdf3f3"),
    "notificacoes": ("#e1d5e7", "#9673a6", "#f8f4fa"),
    "compartilhado": ("#eeeeee", "#999999", "#fafafa"),
}
FONTE = "Helvetica"


def largura(texto: str, px: float) -> float:
    """Estimativa conservadora da largura de um texto em Helvetica."""
    return sum(0.36 if c in "il.,:;|'!()[] " else 0.68 if c.isupper() or c in "mwMW_" else 0.56
               for c in texto) * px


def slug(texto: str) -> str:
    base = unicodedata.normalize("NFKD", texto).encode("ascii", "ignore").decode().lower()
    return "-".join("".join(c if c.isalnum() else " " for c in base).split())


@dataclass(frozen=True)
class Box:
    x: float
    y: float
    w: float
    h: float
    nome: str = ""

    @property
    def x2(self):
        return self.x + self.w

    @property
    def y2(self):
        return self.y + self.h

    @property
    def centro(self):
        return self.x + self.w / 2, self.y + self.h / 2

    def cruza(self, o: "Box", folga: float = 0) -> bool:
        return self.x < o.x2 + folga and o.x < self.x2 + folga and self.y < o.y2 + folga and o.y < self.y2 + folga


def uniao(caixas) -> Box:
    caixas = list(caixas)
    x, y = min(c.x for c in caixas), min(c.y for c in caixas)
    return Box(x, y, max(c.x2 for c in caixas) - x, max(c.y2 for c in caixas) - y)


def segmento_cruza_caixa(a, b, caixa: Box, encolher: float = 1.0) -> bool:
    """Liang-Barsky contra a caixa encolhida (tocar a borda não conta)."""
    x1, y1, x2, y2 = caixa.x + encolher, caixa.y + encolher, caixa.x2 - encolher, caixa.y2 - encolher
    dx, dy = b[0] - a[0], b[1] - a[1]
    t0, t1 = 0.0, 1.0
    for p, q in ((-dx, a[0] - x1), (dx, x2 - a[0]), (-dy, a[1] - y1), (dy, y2 - a[1])):
        if abs(p) < 1e-9:
            if q < 0:
                return False
            continue
        r = q / p
        if p < 0:
            t0 = max(t0, r)
        else:
            t1 = min(t1, r)
        if t0 > t1:
            return False
    return True


def segmentos_cruzam(a, b, c, d) -> bool:
    def ori(p, q, r):
        v = (q[0] - p[0]) * (r[1] - p[1]) - (q[1] - p[1]) * (r[0] - p[0])
        return 0 if abs(v) < 1e-6 else (1 if v > 0 else -1)

    o = (ori(a, b, c), ori(a, b, d), ori(c, d, a), ori(c, d, b))
    return 0 not in o and o[0] != o[1] and o[2] != o[3]


class Pagina:
    def __init__(self, nome: str):
        self.nome = nome
        self.celulas: list[str] = []
        self.ids: set[str] = set()

    def _id(self, ident: str) -> str:
        if ident in self.ids:
            raise ValueError(f"{self.nome}: id duplicado {ident}")
        self.ids.add(ident)
        return ident

    def vertice(self, ident, valor, estilo, box: Box, pai="1", extra=""):
        self.celulas.append(
            f'<mxCell id={quoteattr(self._id(ident))} value={quoteattr(valor)} style={quoteattr(estilo)} '
            f'vertex="1" parent={quoteattr(pai)}{extra}><mxGeometry x="{box.x:g}" y="{box.y:g}" '
            f'width="{box.w:g}" height="{box.h:g}" as="geometry"/></mxCell>')
        return ident

    def aresta(self, ident, origem, destino, estilo, pontos=()):
        assert origem in self.ids and destino in self.ids, ident
        geo = "".join(f'<mxPoint x="{x:g}" y="{y:g}"/>' for x, y in pontos)
        geo = f'<Array as="points">{geo}</Array>' if geo else ""
        self.celulas.append(
            f'<mxCell id={quoteattr(self._id(ident))} style={quoteattr(estilo)} edge="1" parent="1" '
            f'source={quoteattr(origem)} target={quoteattr(destino)}>'
            f'<mxGeometry relative="1" as="geometry">{geo}</mxGeometry></mxCell>')

    def xml(self, w, h) -> str:
        return (f'<diagram name={quoteattr(self.nome)} id={quoteattr(slug(self.nome))}>'
                f'<mxGraphModel dx="1400" dy="900" grid="0" gridSize="10" guides="1" tooltips="1" connect="1" '
                f'arrows="1" fold="1" page="1" pageScale="1" pageWidth="{w:g}" pageHeight="{h:g}" math="0" '
                f'shadow="0" background="#ffffff"><root><mxCell id="0"/><mxCell id="1" parent="0"/>'
                + "".join(self.celulas) + "</root></mxGraphModel></diagram>")


def legenda(pg: Pagina, linhas: list[str], modulos: list[str], x: float, y: float, w: float) -> Box:
    """Caixa de legenda: chave de cores dos módulos + itens de texto."""
    altura_texto = 16 * sum(1 + int(largura(l, 11) // (w - 40)) for l in linhas)
    caixa = Box(x, y, w, 76 + altura_texto)
    pg.vertice("legenda", "", "rounded=1;arcSize=4;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#bbbbbb;",
               caixa)
    pg.vertice("legenda-titulo", "Legenda", f"text;html=1;fontStyle=1;fontSize=12;fontFamily={FONTE};"
               "align=left;verticalAlign=middle;", Box(x + 14, y + 8, 120, 20))
    cx = x + 14
    for m in modulos:
        fill, stroke, _ = PALETA[m]
        pg.vertice(f"legenda-cor-{m}", "", f"rounded=0;html=1;fillColor={fill};strokeColor={stroke};",
                   Box(cx, y + 38, 14, 14))
        pg.vertice(f"legenda-mod-{m}", m, f"text;html=1;fontSize=11;fontFamily={FONTE};align=left;"
                   "verticalAlign=middle;", Box(cx + 18, y + 36, largura(m, 11) + 8, 18))
        cx += largura(m, 11) + 44
    texto = "<br>".join("• " + l.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                         for l in linhas)
    pg.vertice("legenda-texto", texto, f"text;html=1;whiteSpace=wrap;fontSize=11;fontFamily={FONTE};"
               "align=left;verticalAlign=top;spacing=0;", Box(x + 14, y + 64, w - 28, altura_texto))
    return caixa


# ======================================================================================================
# Página lógica (template entity_relationship do draw.io)
# ======================================================================================================

CAB = 26
LINHA = 22
MARCA = 62
FOLGA_COL = 80  # corredor entre colunas de tabelas, onde ficam os "ombros" das linhas
OMBRO = 30       # segmento horizontal do entityRelationEdgeStyle

# Por módulo: colunas de tabelas, cada uma com (tabela, y relativo ao topo do módulo).
LAYOUT_LOGICO = {
    "clientes": [[("endereco", 0)], [("cliente", 0)], [("cartao", 0)], [("bandeira", 22)]],
    "catalogo": [[("editora", 0), ("grupo_precificacao", 110)], [("livro", 0)],
                 [("livro_autor", 0), ("livro_categoria", 110)], [("autor", 0), ("categoria", 110)]],
    "estoque": [[("estoque", 0), ("reserva", 120)], [("entrada_estoque", 0)], [("fornecedor", 44)]],
    "vendas": [
        [("item_carrinho", 0), ("item_removido_carrinho", 150), ("item_checkout", 420)],
        [("carrinho", 0), ("checkout", 398)],
        [("pedido", 376)],
        [("item_pedido", 230), ("troca", 400), ("pagamento_cartao", 620), ("cupom_pedido", 770),
         ("uso_cupom_promocional", 920)],
        [("item_troca", 280), ("cupom", 540), ("cupom_promocional", 880)],
    ],
    "acesso": [[("usuario", 0)]],
    "notificacoes": [[("notificacao", 0)]],
    "compartilhado": [[("revisao", 0), ("seed_execucao", 140)]],
}
# Origem (x, y) de cada módulo na página.
ORIGEM_MODULO = {
    "clientes": (40, 40),
    "catalogo": (1340, 40),
    "estoque": (2640, 40),
    "vendas": (40, 520),
    "acesso": (1880, 520),
    "notificacoes": (2240, 520),
    "compartilhado": (2640, 520),
}
POS_LEGENDA_LOGICO = (1880, 960, 1300)
PAD = 24
PAD_TOPO = 40


def texto_marca(c) -> str:
    return " ".join(m for m in c[2].split() if m in ("PK", "FK", "UK", "UK1", "REF"))


def texto_tipo(c) -> str:
    tipo = c[1] + ("?" if "null" in c[2].split() else "")
    if "→" in c[2]:
        tipo += "  → " + c[2].split("→")[1].split()[0]
    return tipo


def medir(tabela) -> tuple[float, float, float]:
    """Largura, altura e x onde começa a coluna de tipos."""
    nome_w = max(largura(c[0], 12) for c in tabela["colunas"]) + 18
    tipo_w = max(largura(texto_tipo(c), 11) for c in tabela["colunas"]) + 14
    w = max(MARCA + nome_w + tipo_w, largura(tabela["nome"], 12) + 30)
    return w, CAB + LINHA * len(tabela["colunas"]), w - tipo_w


def gerar_logico(mer):
    pg = Pagina("Lógico")
    tabelas = {t["nome"]: (m["id"], t) for m in mer["modulos"] for t in m["tabelas"]}
    caixas: dict[str, Box] = {}
    grupos: dict[str, Box] = {}
    erros = []

    for modulo, colunas in LAYOUT_LOGICO.items():
        ox, oy = ORIGEM_MODULO[modulo]
        x = ox + PAD
        for coluna in colunas:
            for nome, dy in coluna:
                if tabelas[nome][0] != modulo:
                    erros.append(f"{nome} fora do módulo {modulo}")
                w, h, _ = medir(tabelas[nome][1])
                caixas[nome] = Box(x, oy + PAD_TOPO + dy, w, h, nome)
            x += max(medir(tabelas[n][1])[0] for n, _ in coluna) + FOLGA_COL
        g = uniao(caixas[n] for col in colunas for n, _ in col)
        grupos[modulo] = Box(ox, oy, g.x2 + PAD - ox, g.y2 + PAD - oy, modulo)
    erros += [f"tabela sem posição: {n}" for n in set(tabelas) - set(caixas)]

    for modulo, g in grupos.items():
        _, stroke, fundo = PALETA[modulo]
        pg.vertice(f"g-{modulo}", modulo,
                   f"rounded=1;arcSize=3;whiteSpace=wrap;html=1;dashed=1;dashPattern=8 4;fillColor={fundo};"
                   f"strokeColor={stroke};verticalAlign=top;align=left;spacingLeft=12;spacingTop=4;fontStyle=2;"
                   f"fontSize=14;fontFamily={FONTE};fontColor={stroke};", g)

    for nome, caixa in caixas.items():
        modulo, t = tabelas[nome]
        fill, stroke, _ = PALETA[modulo]
        w, _, x_tipo = medir(t)
        tid = pg.vertice(
            f"t-{nome}", nome,
            f"swimlane;html=1;fontStyle=1;childLayout=stackLayout;horizontal=1;startSize={CAB};"
            f"horizontalStack=0;resizeParent=1;resizeLast=0;collapsible=0;marginBottom=0;"
            f"swimlaneFillColor=#ffffff;fillColor={fill};strokeColor={stroke};align=center;rounded=0;shadow=0;"
            f"strokeWidth=1;fontFamily={FONTE};fontSize=12;", caixa)
        n_pk = sum(1 for c in t["colunas"] if "PK" in c[2].split())
        for i, c in enumerate(t["colunas"]):
            pk = "PK" in c[2].split()
            rid = pg.vertice(
                f"c-{nome}-{c[0]}", c[0],
                f"shape=partialRectangle;top=0;left=0;right=0;bottom={int(pk and i == n_pk - 1)};html=1;"
                f"align=left;verticalAlign=middle;fillColor=none;spacingLeft={MARCA + 4};spacingRight=4;"
                f"whiteSpace=wrap;overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;"
                f"dropTarget=0;fontFamily={FONTE};fontSize=12;strokeColor={stroke};"
                + ("fontStyle=5;" if pk else ""),
                Box(0, CAB + i * LINHA, w, LINHA), pai=tid)
            pg.vertice(f"c-{nome}-{c[0]}-marca", texto_marca(c),
                       f"shape=partialRectangle;top=0;left=0;bottom=0;right=1;html=1;fillColor=none;align=left;"
                       f"verticalAlign=middle;spacingLeft=6;spacingRight=2;whiteSpace=wrap;overflow=hidden;"
                       f"rotatable=0;points=[];portConstraint=eastwest;part=1;fontFamily={FONTE};fontSize=10;"
                       f"strokeColor={stroke};" + ("fontStyle=1;" if pk else ""),
                       Box(0, 0, MARCA, LINHA), pai=rid, extra=' connectable="0"')
            pg.vertice(f"c-{nome}-{c[0]}-tipo", texto_tipo(c),
                       "shape=partialRectangle;top=0;left=0;bottom=0;right=0;html=1;fillColor=none;align=left;"
                       "verticalAlign=middle;spacingLeft=4;spacingRight=4;whiteSpace=wrap;overflow=hidden;"
                       f"rotatable=0;points=[];portConstraint=eastwest;part=1;fontFamily={FONTE};fontSize=11;"
                       "fontColor=#666666;", Box(x_tipo, 0, w - x_tipo, LINHA), pai=rid,
                       extra=' connectable="0"')

    caminhos = []
    for r in mer["relacionamentos"]:
        tab_de, col_de = r["de"].split(".")
        tab_para = r["para"]
        de, para = caixas[tab_de], caixas[tab_para]
        if r["card"] == "N:1":
            inicio = "ERoneToMany" if r.get("min") == "1" else "ERzeroToMany"
        else:
            inicio = "ERmandOne" if r.get("min") == "1" else "ERzeroToOne"
        fim = "ERzeroToOne" if r.get("opcional") else "ERmandOne"
        direita = de.centro[0] < para.centro[0]
        pg.aresta(f"r-{tab_de}-{col_de}", f"c-{tab_de}-{col_de}", f"c-{tab_para}-id",
                  f"edgeStyle=entityRelationEdgeStyle;html=1;rounded=1;jumpStyle=arc;jumpSize=8;"
                  f"startArrow={inicio};endArrow={fim};startSize=10;endSize=10;strokeColor=#555555;"
                  f"exitX={int(direita)};exitY=0.5;entryX={int(not direita)};entryY=0.5;")
        i_de = [c[0] for c in tabelas[tab_de][1]["colunas"]].index(col_de)
        y0 = de.y + CAB + LINHA * i_de + LINHA / 2
        y1 = para.y + CAB + LINHA / 2
        x0, x1 = (de.x2, para.x) if direita else (de.x, para.x2)
        s = OMBRO if direita else -OMBRO
        caminhos.append((r["de"], {tab_de, tab_para}, [(x0, y0), (x0 + s, y0), (x1 - s, y1), (x1, y1)]))

    lx, ly, lw = POS_LEGENDA_LOGICO
    leg = legenda(pg, mer["legenda"] + [
        "Pontas (pé de galinha): traço = um; círculo = zero (opcional); três pernas = muitos. "
        "A linha sai da coluna FK e chega à PK da tabela referenciada."], list(PALETA), lx, ly, lw)

    nomes = list(caixas)
    for i, a in enumerate(nomes):
        for b in nomes[i + 1:]:
            if caixas[a].cruza(caixas[b], 20):
                erros.append(f"tabelas sobrepostas: {a} × {b}")
    blocos = list(grupos.values()) + [Box(leg.x, leg.y, leg.w, leg.h, "legenda")]
    for i, a in enumerate(blocos):
        for b in blocos[i + 1:]:
            if a.cruza(b, 10):
                erros.append(f"blocos sobrepostos: {a.nome} × {b.nome}")
    for nome_r, pontas, pts in caminhos:
        for nome, caixa in caixas.items():
            if nome not in pontas and any(segmento_cruza_caixa(a, b, caixa) for a, b in zip(pts, pts[1:])):
                erros.append(f"linha {nome_r} passa sobre a tabela {nome}")
    cruzamentos = sum(
        1 for i, (_, _, p) in enumerate(caminhos) for _, _, q in caminhos[i + 1:]
        for a, b in zip(p, p[1:]) for c, d in zip(q, q[1:]) if segmentos_cruzam(a, b, c, d))
    return pg, max(b.x2 for b in blocos) + 40, max(b.y2 for b in blocos) + 40, erros, cruzamentos


# ======================================================================================================
# Página conceitual (Chen, estilo brModelo)
# ======================================================================================================

ENT_H = 40
LOS_H = 56
PASSO_ATR = 18
CIRC = 10

# Centro de cada entidade e lado dos atributos (left, right, above, below; above_r e below_r alinham o pente à
# borda direita).
ENTIDADES = {
    "Usuário": (140, 560, "above"),
    "Notificação": (140, 1000, "below"),
    "Cliente": (560, 720, "left"),
    "Endereço": (300, 170, "left"),
    "Cartão": (700, 330, "above"),
    "Bandeira": (1000, 330, "above"),
    "Fornecedor": (1000, 150, "right"),
    "Carrinho": (1010, 560, "above"),
    "Checkout": (900, 960, "left"),
    "Pedido": (1180, 1300, "above_r"),
    "Item do pedido": (1700, 1300, "below_r"),
    "Pagamento com cartão": (1180, 1760, "below"),
    "Cupom promocional": (880, 1760, "below"),
    "Troca": (1700, 1760, "right"),
    "Cupom de troca": (1460, 2080, "right"),
    "Livro": (1700, 760, "below_r"),
    "Autor": (2240, 340, "right"),
    "Categoria": (2240, 480, "right"),
    "Editora": (2240, 620, "right"),
    "Grupo de precificação": (2240, 760, "right"),
    "Entrada de estoque": (1330, 300, "above"),
    "Saldo de estoque": (1570, 300, "above"),
    "Reserva": (1810, 300, "above"),
}

# Centro de cada losango; a chave é "nome|entidade A|entidade B" (lados do spec).
LOSANGOS = {
    "recebe|Usuário|Notificação": (140, 800),
    "é|Usuário|Cliente": (350, 610),
    "possui|Cliente|Endereço": (400, 450),
    "possui|Cliente|Cartão": (640, 525),
    "é da|Cartão|Bandeira": (850, 330),
    "escreve|Autor|Livro": (1980, 340),
    "classifica|Categoria|Livro": (1980, 480),
    "publica|Editora|Livro": (1980, 620),
    "precifica|Grupo de precificação|Livro": (1980, 760),
    "recebe|Livro|Entrada de estoque": (1450, 530),
    "fornece|Fornecedor|Entrada de estoque": (1160, 225),
    "tem saldo|Livro|Saldo de estoque": (1610, 530),
    "bloqueia|Livro|Reserva": (1770, 530),
    "tem|Cliente|Carrinho": (790, 640),
    "contém|Carrinho|Livro": (1340, 610),
    "removeu|Carrinho|Livro": (1290, 720),
    "abre|Cliente|Checkout": (730, 840),
    "reúne|Checkout|Livro": (1300, 880),
    "gera|Checkout|Pedido": (1040, 1130),
    "faz|Cliente|Pedido": (820, 1300),
    "compõe|Pedido|Item do pedido": (1440, 1300),
    "refere|Item do pedido|Livro": (1700, 1030),
    "paga|Pedido|Pagamento com cartão": (1180, 1530),
    "usa|Pedido|Cupom promocional": (950, 1530),
    "abate|Pedido|Cupom de troca": (1300, 1620),
    "pertence|Cliente|Cupom de troca": (560, 2080),
    "solicita|Pedido|Troca": (1460, 1530),
    "inclui|Troca|Item do pedido": (1700, 1530),
    "gera|Troca|Cupom de troca": (1640, 1960),
}
# Pontos intermediários da linha losango→entidade: {(chave, índice do lado): [(x, y), ...]}.
DESVIOS: dict[tuple[str, int], list[tuple[float, float]]] = {}
# Lado dos atributos de relacionamento (padrão: below).
LADO_ATR_REL: dict[str, str] = {
    "usa|Pedido|Cupom promocional": "right",
    "abate|Pedido|Cupom de troca": "right",
    "inclui|Troca|Item do pedido": "right",
}
# Hubs mais largos: as linhas chegam em pontos mais espaçados da borda, com lugar para as cardinalidades.
LARGURA_ENTIDADE = {"Livro": 240, "Cliente": 170, "Pedido": 170}
LARGURA_LEGENDA_CONCEITUAL = 1300


def chave_rel(rel) -> str:
    return "|".join([rel["nome"]] + [e for e, _ in rel["lados"]])


def caixa_entidade(nome) -> Box:
    x, y, _ = ENTIDADES[nome]
    w = LARGURA_ENTIDADE.get(nome, max(110, largura(nome, 12) + 28))
    return Box(x - w / 2, y - ENT_H / 2, w, ENT_H, nome)


def caixa_losango(rel) -> Box:
    x, y = LOSANGOS[chave_rel(rel)]
    w = max(96, largura(rel["nome"], 12) + 50)
    return Box(x - w / 2, y - LOS_H / 2, w, LOS_H, rel["nome"])


def atributos(pg: Pagina, dono_id: str, dono: Box, nomes: list[str], lado: str, prefixo: str):
    """Pirulitos (círculo + texto) em pente; devolve caixas e segmentos para a verificação."""
    caixas, segs = [], []
    for i, nome in enumerate(nomes):
        ident = nome.startswith("#")
        texto = nome.lstrip("#")
        tw = largura(texto, 10) + 6
        if lado in ("left", "right"):
            dx = 1 if lado == "right" else -1
            borda = dono.x2 if lado == "right" else dono.x
            tronco = borda + dx * 12
            cx, cy = borda + dx * 26, dono.y + 8 + i * PASSO_ATR
            rot = Box(cx + 9, cy - 8, tw, 16) if lado == "right" else Box(cx - 9 - tw, cy - 8, tw, 16)
            pts = [(tronco, dono.centro[1]), (tronco, cy)]
            portas = f"exitX={int(lado == 'right')};exitY=0.5;entryX={int(lado == 'left')};entryY=0.5;"
            seg = [(borda, dono.centro[1]), (tronco, dono.centro[1]), (tronco, cy), (cx, cy)]
        else:
            dy = 1 if lado.startswith("below") else -1
            borda = dono.y2 if dy == 1 else dono.y
            # *_r: pente junto à borda direita, textos fora da entidade
            tronco = dono.x2 - 14 if lado.endswith("_r") else dono.x + 14
            cx, cy = tronco + 14, borda + dy * (16 + i * PASSO_ATR)
            rot = Box(cx + 9, cy - 8, tw, 16)
            pts = [(tronco, cy)]
            portas = f"exitX={(tronco - dono.x) / dono.w:.4f};exitY={int(lado.startswith('below'))};entryX=0;entryY=0.5;"
            seg = [(tronco, borda), (tronco, cy), (cx, cy)]
        circ = Box(cx - CIRC / 2, cy - CIRC / 2, CIRC, CIRC)
        cid = pg.vertice(f"{prefixo}-{i}", "", "ellipse;html=1;aspect=fixed;strokeColor=#333333;"
                         f"fillColor={'#222222' if ident else '#ffffff'};", circ)
        pg.vertice(f"{prefixo}-{i}-txt", texto, f"text;html=1;fontSize=10;fontFamily={FONTE};"
                   f"align={'right' if lado == 'left' else 'left'};verticalAlign=middle;fontColor=#333333;",
                   rot)
        pg.aresta(f"{prefixo}-{i}-l", dono_id, cid, "endArrow=none;startArrow=none;html=1;rounded=0;"
                  f"edgeStyle=none;strokeColor=#666666;{portas}", pts)
        caixas += [circ, rot]
        segs.append(seg)
    return caixas, segs


def ponto_na_borda(caixa: Box, de) -> tuple[float, float]:
    """Interseção da reta centro→de com a borda da caixa."""
    cx, cy = caixa.centro
    dx, dy = de[0] - cx, de[1] - cy
    t = min(abs(caixa.w / 2 / dx) if dx else math.inf, abs(caixa.h / 2 / dy) if dy else math.inf)
    return cx + dx * t, cy + dy * t


def gerar_conceitual(con):
    pg = Pagina("Conceitual")
    solidos: list[tuple[str, Box]] = []        # caixas que nada pode sobrepor nem atravessar
    linhas: list[tuple[str, set, list]] = []   # (descrição, donos a ignorar, pontos)
    rotulos: list[tuple[str, str, str, list]] = []

    ents = {}
    for e in con["entidades"]:
        caixa = caixa_entidade(e["nome"])
        ents[e["nome"]] = caixa
        fill, stroke, _ = PALETA[e["grupo"]]
        estilo = (f"rounded=0;whiteSpace=wrap;html=1;fontStyle=1;fontSize=12;fontFamily={FONTE};"
                  f"fillColor={fill};strokeColor={stroke};strokeWidth=1.5;")
        if e.get("fraca"):
            estilo = "shape=ext;double=1;margin=3;" + estilo
        eid = pg.vertice(f"e-{slug(e['nome'])}", e["nome"], estilo, caixa)
        solidos.append((e["nome"], caixa))
        cx, segs = atributos(pg, eid, caixa, e["atributos"], ENTIDADES[e["nome"]][2], f"a-{slug(e['nome'])}")
        solidos += [(f"atr:{e['nome']}", c) for c in cx]
        linhas += [(f"atributo de {e['nome']}", {e["nome"], f"atr:{e['nome']}"}, s) for s in segs]

    for rel in con["relacionamentos"]:
        chave = chave_rel(rel)
        caixa = caixa_losango(rel)
        estilo = (f"whiteSpace=wrap;html=1;fontSize=12;fontFamily={FONTE};fillColor=#ffffff;"
                  "strokeColor=#333333;strokeWidth=1.5;")
        estilo = ("shape=rhombus;double=1;margin=4;" if rel.get("identificador") else "rhombus;") + estilo
        lid = pg.vertice(f"r-{slug(chave)}", rel["nome"], estilo, caixa)
        solidos.append((chave, caixa))
        if rel.get("atributos"):
            cx, segs = atributos(pg, lid, caixa, rel["atributos"], LADO_ATR_REL.get(chave, "below"),
                                 f"ra-{slug(chave)}")
            solidos += [(f"atr:{chave}", c) for c in cx]
            linhas += [(f"atributo de {chave}", {chave, f"atr:{chave}"}, s) for s in segs]
        for i, (ent, card) in enumerate(rel["lados"]):
            eb = ents[ent]
            meio = DESVIOS.get((chave, i), [])
            p_los = ponto_na_borda(caixa, meio[0] if meio else eb.centro)
            p_ent = ponto_na_borda(eb, meio[-1] if meio else caixa.centro)
            pg.aresta(f"r-{slug(chave)}-{i}", lid, f"e-{slug(ent)}",
                      "endArrow=none;startArrow=none;html=1;rounded=0;edgeStyle=none;strokeColor=#333333;"
                      "strokeWidth=1.3;", meio)
            pts = [p_los] + meio + [p_ent]
            linhas.append((f"{rel['nome']} ({ent})", {chave, ent}, pts))
            rotulos.append((f"r-{slug(chave)}-{i}-card", f"card:{chave}:{i}", f"({card})", pts))

    # cardinalidades: depois de todas as linhas, cada rótulo fica na primeira posição livre junto à entidade
    segs_todos = [(a, b) for _, _, pts in linhas for a, b in zip(pts, pts[1:])]
    for ident, nome, texto, pts in rotulos:
        p_ent, ax, ay = pts[-1], *pts[-2]
        n = math.hypot(ax - p_ent[0], ay - p_ent[1]) or 1
        ux, uy = (ax - p_ent[0]) / n, (ay - p_ent[1]) / n
        tw = largura(texto, 11) + 6
        afasta = abs(uy) * tw / 2 + abs(ux) * 8 + 3     # meia-largura da caixa na direção perpendicular
        avanco = abs(ux) * tw / 2 + abs(uy) * 8 + 4     # meia-largura da caixa ao longo da linha
        escolhido = None
        for passo in (0, 8, 16, 26, 38, 52, 68, 86, 106):
            for lado in (1, -1):
                cx_ = p_ent[0] + ux * (avanco + passo) - uy * lado * afasta
                cy_ = p_ent[1] + uy * (avanco + passo) + ux * lado * afasta
                rot = Box(cx_ - tw / 2, cy_ - 8, tw, 16)
                livre = (not any(rot.cruza(b, 3) for _, b in solidos)
                         and not any(segmento_cruza_caixa(a, b, rot) for a, b in segs_todos))
                if livre:
                    escolhido = rot
                    break
            if escolhido:
                break
        rot = escolhido or Box(p_ent[0] + ux * avanco - tw / 2, p_ent[1] + uy * avanco - 8, tw, 16)
        pg.vertice(ident, texto, f"text;html=1;fontSize=11;fontFamily={FONTE};align=center;"
                   "verticalAlign=middle;", rot)
        solidos.append((nome, rot))

    alto = max(b.y2 for _, b in solidos)
    leg = legenda(pg, con["legenda"], [m for m in PALETA if m != "compartilhado"], 40, alto + 50,
                  LARGURA_LEGENDA_CONCEITUAL)
    solidos.append(("legenda", leg))

    erros = []
    for i, (na, a) in enumerate(solidos):
        for nb, b in solidos[i + 1:]:
            if a.cruza(b, 2):
                erros.append(f"sobreposição: {na} × {nb}")
    for nome, ignorar, pts in linhas:
        for nb, b in solidos:
            if nb in ignorar:
                continue
            if any(segmento_cruza_caixa(a, c, b) for a, c in zip(pts, pts[1:])):
                erros.append(f"linha {nome} atravessa {nb}")
    rel_linhas = [p for n, _, p in linhas if not n.startswith("atributo")]
    cruzamentos = sum(1 for i, p in enumerate(rel_linhas) for q in rel_linhas[i + 1:]
                      for a, b in zip(p, p[1:]) for c, d in zip(q, q[1:]) if segmentos_cruzam(a, b, c, d))
    todas = [b for _, b in solidos]
    return pg, max(b.x2 for b in todas) + 40, max(b.y2 for b in todas) + 40, sorted(set(erros)), cruzamentos


# ======================================================================================================

def conferir_conteudo(xml: str, con, mer) -> list[str]:
    raiz = ET.fromstring(xml)
    erros = []
    diagramas = raiz.findall("diagram")
    if [d.get("name") for d in diagramas] != ["Conceitual", "Lógico"]:
        return ["páginas esperadas: Conceitual, Lógico"]
    for d in diagramas:
        ids = {}
        for c in d.iter("mxCell"):
            if c.get("id") in ids:
                erros.append(f"id repetido {c.get('id')}")
            ids[c.get("id")] = c
        for c in ids.values():
            if c.get("edge") == "1" and (c.get("source") not in ids or c.get("target") not in ids):
                erros.append(f"aresta {c.get('id')} sem ponta")
    v1 = {c.get("value") for c in diagramas[0].iter("mxCell")}
    for e in con["entidades"]:
        erros += [f"conceitual: falta {v}" for v in [e["nome"]] + [a.lstrip("#") for a in e["atributos"]]
                  if v not in v1]
    for r in con["relacionamentos"]:
        erros += [f"conceitual: falta {v}" for v in [r["nome"]] + r.get("atributos", []) if v not in v1]
    v2 = {c.get("value") for c in diagramas[1].iter("mxCell")}
    for m in mer["modulos"]:
        for t in m["tabelas"]:
            erros += [f"lógico: falta {t['nome']}.{c[0]}" for c in t["colunas"] if c[0] not in v2]
    n = sum(1 for c in diagramas[1].iter("mxCell") if c.get("edge") == "1")
    if n != len(mer["relacionamentos"]):
        erros.append(f"lógico: {n} linhas para {len(mer['relacionamentos'])} relacionamentos")
    return erros


def main() -> int:
    con = json.loads((BASE / "conceitual.spec.json").read_text(encoding="utf-8"))
    mer = json.loads((BASE / "mer.spec.json").read_text(encoding="utf-8"))
    p1, w1, h1, e1, x1 = gerar_conceitual(con)
    p2, w2, h2, e2, x2 = gerar_logico(mer)
    xml = ('<?xml version="1.0" encoding="UTF-8"?>\n<mxfile host="gerar_mer.py">'
           + p1.xml(w1, h1) + p2.xml(w2, h2) + "</mxfile>\n")
    erros = e1 + e2 + conferir_conteudo(xml, con, mer)
    print(f"Conceitual: {len(con['entidades'])} entidades, {len(con['relacionamentos'])} relacionamentos, "
          f"{x1} cruzamento(s) de linhas")
    print(f"Lógico: {sum(len(m['tabelas']) for m in mer['modulos'])} tabelas, {len(mer['relacionamentos'])} "
          f"linhas, {x2} cruzamento(s) de linhas")
    if erros:
        print(f"{len(erros)} problema(s):")
        for e in erros:
            print("  -", e)
        if "--forcar" not in sys.argv:
            return 1
    SAIDA.write_text(xml, encoding="utf-8")
    print(f"gravado {SAIDA.name}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
