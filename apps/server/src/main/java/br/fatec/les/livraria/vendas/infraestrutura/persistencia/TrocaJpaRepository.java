package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import jakarta.persistence.LockModeType;

public interface TrocaJpaRepository extends JpaRepository<TrocaJpa, Long>, JpaSpecificationExecutor<TrocaJpa> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TrocaJpa t where t.id = :id")
    Optional<TrocaJpa> buscarPorIdComLock(@Param("id") Long id);

    /** A página da consulta do admin, com o pedido de cada troca para a cópia do cliente (RF-VEN-013). */
    @Override
    @EntityGraph(attributePaths = "pedido")
    Page<TrocaJpa> findAll(@Nullable Specification<TrocaJpa> especificacao, Pageable paginacao);

    /** Total de unidades de cada troca, para a consulta do admin sem carregar os itens na paginação. */
    @Query("select i.troca.id as trocaId, sum(i.quantidade) as unidades from ItemTrocaJpa i "
            + "where i.troca.id in :ids group by i.troca.id")
    List<UnidadesDaTroca> somarUnidades(@Param("ids") Collection<Long> ids);

    interface UnidadesDaTroca {

        Long getTrocaId();

        Long getUnidades();
    }

    @EntityGraph(attributePaths = "itens")
    List<TrocaJpa> findByPedidoId(Long pedidoId);

    /**
     * Quantidade trocada de cada item dos pedidos finalizados no período com um dos status, só nas trocas com o
     * status informado (RN-ANA-004, DEC-12).
     */
    @Query("select i.itemPedido.id as itemId, sum(i.quantidade) as quantidade from ItemTrocaJpa i "
            + "join i.troca t join t.pedido p where t.status = :statusDaTroca and p.status in :status "
            + "and p.realizadoEm >= :inicio and p.realizadoEm < :fim group by i.itemPedido.id")
    List<QuantidadeTrocada> somarTrocadasDoPeriodo(@Param("statusDaTroca") StatusTroca statusDaTroca,
            @Param("status") Collection<StatusPedido> status, @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fimExclusivo);

    /**
     * Soma dos cupons gerados pelas trocas com o status informado, por pedido dos clientes finalizado desde o
     * instante com um dos status (RN-CLI-007, DEC-12).
     */
    @Query("select t.pedido.id as pedidoId, sum(t.valorCupom) as valor from TrocaJpa t "
            + "where t.status = :statusDaTroca and t.pedido.clienteId in :clientes and t.pedido.status in :status "
            + "and t.pedido.realizadoEm >= :desde group by t.pedido.id")
    List<CuponsDoPedido> somarCuponsDosClientesDesde(@Param("statusDaTroca") StatusTroca statusDaTroca,
            @Param("clientes") Collection<Long> clienteIds, @Param("status") Collection<StatusPedido> status,
            @Param("desde") LocalDateTime desde);

    interface CuponsDoPedido {

        Long getPedidoId();

        BigDecimal getValor();
    }

    interface QuantidadeTrocada {

        Long getItemId();

        Long getQuantidade();
    }

    @Query("select t.status as status, count(t) as quantidade from TrocaJpa t group by t.status")
    List<QuantidadePorStatus<StatusTroca>> contarPorStatus();
}
