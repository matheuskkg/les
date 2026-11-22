package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDeChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.UltimaFalha;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Leituras de {@code chamada_ia} e das avaliações para a tela do ADMIN (RNF-REC-001, DEC-03). */
public interface MetricasIaRepositorio {

    /** Tentativas iniciadas em {@code [de, ate)}, de todos os provedores. */
    List<ChamadaIa> chamadas(LocalDateTime de, LocalDateTime ate);

    /**
     * Avaliações dos itens recomendados e das respostas do assistente geradas por chamadas iniciadas em {@code [de,
     * ate)}, com o provedor da chamada.
     */
    List<AvaliacaoDeChamada> avaliacoes(LocalDateTime de, LocalDateTime ate);

    /** Recomendações da vitrine gravadas sem IA em {@code [de, ate)}. */
    long recomendacoesSemIa(LocalDateTime de, LocalDateTime ate);

    /** Última falha de cada provedor que já falhou, pelo id do provedor. */
    Map<Long, UltimaFalha> ultimasFalhas();
}
