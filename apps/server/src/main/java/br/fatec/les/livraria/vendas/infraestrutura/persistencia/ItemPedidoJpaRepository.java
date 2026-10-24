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

    interface ItemDoPeriodo {

        Long getId();

        LivroNoPedido getLivro();

        int getQuantidade();

        BigDecimal getPrecoUnitario();

        LocalDateTime getRealizadoEm();
    }
}
