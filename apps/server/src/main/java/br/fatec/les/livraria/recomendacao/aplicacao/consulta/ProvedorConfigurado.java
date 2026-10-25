package br.fatec.les.livraria.recomendacao.aplicacao.consulta;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.UltimaFalha;

/**
 * Provedor da cadeia como o ADMIN o vê (RNF-REC-001, DEC-03): a linha de {@code provedor_ia}, o modelo, se há chave
 * configurada e a última falha. A chave e a URL nunca saem da configuração.
 *
 * @param provedor         ordem e ativação gravadas
 * @param modelo           modelo do adapter, ou {@code null} se nenhum adapter tem esse nome
 * @param chaveConfigurada se o adapter tem chave; sem adapter, {@code false}
 * @param ultimaFalha      última tentativa sem sucesso, de qualquer período
 */
public record ProvedorConfigurado(
        ProvedorIa provedor,
        @Nullable String modelo,
        boolean chaveConfigurada,
        @Nullable UltimaFalha ultimaFalha) {
}
