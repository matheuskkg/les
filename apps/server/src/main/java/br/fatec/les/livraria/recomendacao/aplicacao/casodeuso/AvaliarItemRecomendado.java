package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RecomendacoesRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Recomendacao;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Avaliação positiva ou negativa de um item de "Recomendados para você" (RNF-REC-001, DEC-03). A última vale. O item
 * avaliado negativamente some da vitrine na hora, sem gerar outra recomendação, e o livro sai dos candidatos seguintes.
 * Item de outro cliente responde como inexistente.
 */
@Component
@RequiredArgsConstructor
public class AvaliarItemRecomendado {

    private final IdentificarCliente identificarCliente;
    private final RecomendacoesRepositorio recomendacoes;
    private final Relogio relogio;

    @Transactional
    public void executar(Long itemId, Avaliacao avaliacao) {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(avaliacao, "avaliacao");
        Long clienteId = identificarCliente.executar();
        Recomendacao recomendacao =
                recomendacoes
                        .buscarPorItem(itemId)
                        .filter(encontrada -> encontrada.clienteId().equals(clienteId))
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Item recomendado não encontrado."));
        ItemDaRecomendacao item =
                recomendacao.itens().stream()
                        .filter(atual -> itemId.equals(atual.id()))
                        .findFirst()
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Item recomendado não encontrado."));
        recomendacoes.salvar(recomendacao.comItem(item.avaliar(avaliacao, relogio.agora())));
    }
}
