package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.fatec.les.livraria.vendas.dominio.pedido.LivroNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

public interface ItemPedidoJpaRepository extends JpaRepository<ItemPedidoJpa, Long> {

    /** Itens dos pedidos finalizados no período com um dos status, para a análise de vendas (RN-ANA-001). */
    @Query("select i.id as id, i.livro as livro, i.quantidade as quantidade, i.precoUnitario as precoUnitario, "
            + "p.realizadoEm as realizadoEm from ItemPedidoJpa i join i.pedido p "
            + "where p.status in :status and p.realizadoEm >= :inicio and p.realizadoEm < :fim")
    List<ItemDoPeriodo> buscarDoPeriodo(@Param("status") Collection<StatusPedido> status,
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fimExclusivo);

    /**
     * Itens dos pedidos de um cliente com um dos status, com o pedido e o cliente, para o perfil e o histórico do
     * chat (RNF-REC-001). O filtro por cliente fica no banco, no índice {@code ix_pedido_cliente} (RNF-GER-001).
     */
    @Query("select i.id as id, i.livro as livro, i.quantidade as quantidade, p.id as pedidoId, "
            + "p.clienteId as clienteId, p.realizadoEm as realizadoEm from ItemPedidoJpa i join i.pedido p "
            + "where p.status in :status and p.clienteId = :cliente")
    List<ItemDoCliente> buscarDoCliente(@Param("status") Collection<StatusPedido> status,
            @Param("cliente") Long clienteId);

    /** Unidades vendidas de cada livro nos pedidos com um dos status, já somadas no banco (RNF-REC-001, RNF-GER-001). */
    @Query("select i.livro as livro, sum(i.quantidade) as quantidade from ItemPedidoJpa i join i.pedido p "
            + "where p.status in :status group by i.livro")
    List<UnidadeDoLivro> somarVendidas(@Param("status") Collection<StatusPedido> status);

    /**
     * Compras de todos os clientes nos pedidos com um dos status, somadas no banco por cliente e livro
     * (RNF-REC-001, RNF-GER-001). O id do livro vem da cópia no pedido, que é a única fonte: não há
     * {@code livro_id} no item (DEC-16).
     */
    @Query("select p.clienteId as clienteId, i.livro as livro, sum(i.quantidade) as quantidade "
            + "from ItemPedidoJpa i join i.pedido p where p.status in :status group by p.clienteId, i.livro")
    List<CompraDoCliente> somarCompras(@Param("status") Collection<StatusPedido> status);

    /** Unidades vendidas de um livro, agregadas por cliente ou não (RNF-REC-001). */
    interface UnidadeDoLivro {

        LivroNoPedido getLivro();

        Long getQuantidade();
    }

    /** Compra agregada de um livro por cliente, sem o pedido de origem (RNF-REC-001). */
    interface CompraDoCliente {

        Long getClienteId();

        LivroNoPedido getLivro();

        Long getQuantidade();
    }

    interface ItemDoCliente {

        Long getId();

        LivroNoPedido getLivro();

        int getQuantidade();

        Long getPedidoId();

        Long getClienteId();

        LocalDateTime getRealizadoEm();
    }

    interface ItemDoPeriodo {

        Long getId();

        LivroNoPedido getLivro();

        int getQuantidade();

        BigDecimal getPrecoUnitario();

        LocalDateTime getRealizadoEm();
    }
}
