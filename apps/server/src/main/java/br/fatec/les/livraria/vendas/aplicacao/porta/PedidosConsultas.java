package br.fatec.les.livraria.vendas.aplicacao.porta;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.vendas.aplicacao.consulta.FiltroPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoResumo;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

import java.util.Map;
import java.util.Optional;

/**
 * Porta de saída das consultas de leitura de pedidos do admin (RF-VEN-009, DEC-07). A implementação traduz o filtro
 * para especificações JPA; itens, cartões e cupons só são carregados no detalhe.
 */
public interface PedidosConsultas {

    /**
     * @param filtro campos combinados com E; status repetidos combinam com OU
     * @param paginacao página, tamanho e ordenação já validados
     * @return a página de pedidos que atende ao filtro
     */
    Pagina<PedidoResumo> listar(FiltroPedido filtro, Paginacao paginacao);

    /**
     * @param id número do pedido
     * @return o pedido com todas as cópias, sem trava, ou vazio se não existir
     */
    Optional<Pedido> buscar(Long id);

    /**
     * @return quantidade de pedidos de cada status presente; status sem pedido ficam de fora
     */
    Map<StatusPedido, Long> contarPorStatus();
}
