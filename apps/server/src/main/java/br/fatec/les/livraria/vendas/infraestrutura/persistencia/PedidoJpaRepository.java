package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpa, Long>, JpaSpecificationExecutor<PedidoJpa> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoJpa p where p.id = :id")
    Optional<PedidoJpa> buscarPorIdComLock(@Param("id") Long id);

    /**
     * O detalhe com os itens na mesma consulta (RF-VEN-009, RNF-GER-001, no padrão do {@link
     * TrocaJpaRepository#findByPedidoId}). Cartões e cupons ficam para consultas próprias: são {@code List} sem coluna
     * de ordem, e o Hibernate recusa o fetch de dois bags na mesma consulta ({@code MultipleBagFetchException}).
     */
    @EntityGraph(attributePaths = "itens")
    @Query("select p from PedidoJpa p where p.id = :id")
    Optional<PedidoJpa> buscarPorIdComItens(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoJpa p where p.checkout.id = :checkoutId and p.status = :status")
    Optional<PedidoJpa> buscarPorCheckoutComLock(
            @Param("checkoutId") Long checkoutId, @Param("status") StatusPedido status);

    /** Pedidos dos clientes finalizados desde o instante, com um dos status, para o ranking (RN-CLI-007). */
    @Query(
            "select p.id as id, p.clienteId as clienteId, p.realizadoEm as realizadoEm, p.total as total "
                    + "from PedidoJpa p where p.clienteId in :clientes and p.status in :status and p.realizadoEm >= :desde")
    List<PedidoDoCliente> buscarDosClientesDesde(
            @Param("clientes") Collection<Long> clienteIds,
            @Param("status") Collection<StatusPedido> status,
            @Param("desde") LocalDateTime desde);

    @Query("select p.status as status, count(p) as quantidade from PedidoJpa p group by p.status")
    List<QuantidadePorStatus<StatusPedido>> contarPorStatus();

    interface PedidoDoCliente {

        Long getId();

        Long getClienteId();

        LocalDateTime getRealizadoEm();

        BigDecimal getTotal();
    }
}
