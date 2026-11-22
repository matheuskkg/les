package br.fatec.les.livraria.recomendacao.dominio.modelo;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Recomendação da vitrine gerada para um cliente (RNF-REC-001, DEC-03). Sem {@code chamadaIaId}, foi gravada sem IA, na
 * ordem da pontuação dos candidatos.
 *
 * @param id id da recomendação, nulo antes de gravar
 * @param clienteId cliente da recomendação
 * @param chamadaIaId chamada que gerou a recomendação, ou nula
 * @param geradaEm quando foi gerada, pelo relógio da aplicação
 * @param comprasConsideradas pedidos do cliente que contavam como venda na geração
 * @param itens livros na ordem da vitrine
 */
public record Recomendacao(
        @Nullable Long id,
        Long clienteId,
        @Nullable Long chamadaIaId,
        LocalDateTime geradaEm,
        int comprasConsideradas,
        List<ItemDaRecomendacao> itens) {

    public Recomendacao {
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(geradaEm, "geradaEm");
        itens = List.copyOf(itens);
    }

    /** Gravada sem IA, na ordem da pontuação, porque a cadeia falhou ou nenhum id da resposta valeu. */
    public boolean semIa() {
        return chamadaIaId == null;
    }

    /** Recomendação com o item informado trocado pela versão avaliada. */
    public Recomendacao comItem(ItemDaRecomendacao avaliado) {
        return new Recomendacao(
                id,
                clienteId,
                chamadaIaId,
                geradaEm,
                comprasConsideradas,
                itens.stream().map(item -> Objects.equals(item.id(), avaliado.id()) ? avaliado : item).toList());
    }
}
