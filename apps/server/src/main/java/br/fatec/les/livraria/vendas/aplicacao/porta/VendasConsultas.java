package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import br.fatec.les.livraria.clientes.dominio.ranking.CompraDoCliente;

import br.fatec.les.livraria.vendas.aplicacao.consulta.CompraDeLivro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemVendido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;

/**
 * Porta de saída das vendas consideradas na análise (RF-ANA-001, RN-ANA-004), no ranking (RN-CLI-007) e na
 * recomendação (RNF-REC-001), DEC-12. As consultas da recomendação filtram e agrupam no banco (RNF-GER-001): só os
 * itens do cliente, e o restante do histórico já somado por cliente e livro.
 */
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
     * @param clienteId cliente das compras
     * @return os itens dos pedidos do cliente cujo status conta como venda, com a quantidade menos a trocada em
     *         trocas concluídas; itens trocados por inteiro não aparecem (RNF-REC-001, DEC-03)
     */
    List<LivroVendido> comprasDoCliente(Long clienteId);

    /**
     * @return as compras de todos os clientes cujo status conta como venda, já somadas por par cliente–livro–cópia
     *         do livro no pedido, com a quantidade menos a trocada em trocas concluídas; pares trocados por inteiro
     *         não aparecem (RNF-REC-001, DEC-03). O agrupamento é pela cópia em JSON do snapshot (DEC-16), então
     *         cópias divergentes do mesmo livro — caso teórico enquanto a edição de livros está fora do MVP —
     *         sairiam em registros distintos do mesmo par cliente–livro; quem consome, a co-compra
     *         ({@code ContarCoCompras}), mapeia para conjunto por cliente e é imune a essa multiplicidade.
     */
    List<CompraDeLivro> comprasRegistradas();

    /**
     * @return unidades vendidas por livro, com a quantidade menos a trocada em trocas concluídas; livro sem saldo
     *         líquido não aparece (RNF-REC-001, DEC-03)
     */
    Map<Long, Integer> unidadesVendidasPorLivro();
}
