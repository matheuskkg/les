package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.time.LocalDateTime;
import java.util.List;

import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemVendido;

/** Porta de saída dos itens vendidos num período, para a análise de vendas (RF-ANA-001, RN-ANA-004, DEC-12). */
public interface VendasConsultas {

    /**
     * @param inicio início do período, inclusive
     * @param fimExclusivo fim do período, exclusive
     * @return os itens dos pedidos finalizados no período cujo status conta como venda, com a quantidade em trocas
     *         concluídas
     */
    List<ItemVendido> itensVendidos(LocalDateTime inicio, LocalDateTime fimExclusivo);
}
