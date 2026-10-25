package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import java.util.List;
import java.util.Optional;

import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Recomendacao;

/** Porta de saída das recomendações da vitrine (RNF-REC-001, DEC-03). */
public interface RecomendacoesRepositorio {

    /** A recomendação mais recente do cliente, que é a exibida na vitrine. */
    Optional<Recomendacao> maisRecente(Long clienteId);

    /** A recomendação que contém o item, de qualquer cliente; quem chama confere o dono. */
    Optional<Recomendacao> buscarPorItem(Long itemId);

    /** Grava uma recomendação nova ou as avaliações de uma existente, em transação própria. */
    Recomendacao salvar(Recomendacao recomendacao);

    /**
     * Última avaliação de cada livro recomendado ao cliente, em todas as recomendações (DEC-03).
     *
     * @return uma avaliação por livro
     */
    List<AvaliacaoDada> avaliacoesDoCliente(Long clienteId);
}
