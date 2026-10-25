package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import br.fatec.les.livraria.clientes.dominio.ranking.CompraDoCliente;

import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemVendido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;

/** Porta de saída das vendas consideradas na análise (RF-ANA-001, RN-ANA-004) e no ranking (RN-CLI-007), DEC-12. */
public interface VendasConsultas {

    /**
     * @param inicio início do período, inclusive
     * @param fimExclusivo fim do período, exclusive
     * @return os itens dos pedidos finalizados no período cujo status conta como venda, com a quantidade em trocas
     *         concluídas
     */
    List<ItemVendido> itensVendidos(LocalDateTime inicio, LocalDateTime fimExclusivo);

    /**
     * @param clienteIds clientes desejados
     * @param desde início do período, inclusive
     * @return as compras de cada cliente cujo status conta como venda, com o total menos os cupons das trocas
     *         concluídas do pedido (RN-CLI-007, DEC-04, DEC-12)
     */
    Map<Long, List<CompraDoCliente>> comprasDosClientes(Collection<Long> clienteIds, LocalDateTime desde);

    /**
     * @return os livros de todos os pedidos cujo status conta como venda, com a quantidade menos a trocada em trocas
     *         concluídas; itens trocados por inteiro não aparecem (RNF-REC-001, DEC-03)
     */
    List<LivroVendido> livrosVendidos();
}
