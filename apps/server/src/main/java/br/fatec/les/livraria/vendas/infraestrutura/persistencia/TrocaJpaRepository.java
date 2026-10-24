package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

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
}
