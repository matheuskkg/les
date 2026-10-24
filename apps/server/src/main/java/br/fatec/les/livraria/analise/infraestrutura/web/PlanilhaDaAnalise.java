package br.fatec.les.livraria.analise.infraestrutura.web;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

import br.fatec.les.livraria.analise.dominio.modelo.AnaliseDeVendas;
import br.fatec.les.livraria.analise.dominio.modelo.SerieDeVendas;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * CSV da exportação da análise (RF-ANA-004, DEC-15.4), para abrir direto no Excel em português: UTF-8 com BOM,
 * separador {@code ;}, colunas {@code mes} ({@code AAAA-MM}), {@code categoria} e {@code valor} com vírgula decimal e
 * sem separador de milhar. Uma linha por mês e categoria, inclusive as de R$ 0,00 (RN-ANA-003), ordenadas por mês e,
 * dentro do mês, na ordem das categorias do gráfico.
 */
final class PlanilhaDaAnalise {

    private static final String BOM = "﻿";
    private static final String FIM_DE_LINHA = "\r\n";

    private PlanilhaDaAnalise() {
    }

    static byte[] csv(AnaliseDeVendas analise) {
        StringBuilder csv = new StringBuilder(BOM).append("mes;categoria;valor").append(FIM_DE_LINHA);
        List<YearMonth> meses = analise.periodo().meses();
        for (int i = 0; i < meses.size(); i++) {
            for (SerieDeVendas serie : analise.series()) {
                csv.append(meses.get(i)).append(';')
                        .append(texto(serie.categoria().nome())).append(';')
                        .append(valor(serie.valores().get(i))).append(FIM_DE_LINHA);
            }
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String valor(Dinheiro dinheiro) {
        return dinheiro.valor().toPlainString().replace('.', ',');
    }

    /**
     * Aspas só quando o texto tem separador, aspas ou quebra de linha, dobrando as aspas internas (RFC 4180). Um texto
     * que começa como fórmula ({@code = + - @}) ganha um apóstrofo, para o Excel não executá-lo.
     */
    private static String texto(String original) {
        String valor = original.isEmpty() || "=+-@\t\r".indexOf(original.charAt(0)) < 0 ? original : "'" + original;
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return '"' + valor.replace("\"", "\"\"") + '"';
        }
        return valor;
    }
}
