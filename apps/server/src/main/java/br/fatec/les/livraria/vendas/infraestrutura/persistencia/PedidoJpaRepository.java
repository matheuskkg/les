package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import jakarta.persistence.LockModeType;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpa, Long>, JpaSpecificationExecutor<PedidoJpa> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoJpa p where p.id = :id")
    Optional<PedidoJpa> buscarPorIdComLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoJpa p where p.checkout.id = :checkoutId and p.status = :status")
    Optional<PedidoJpa> buscarPorCheckoutComLock(@Param("checkoutId") Long checkoutId,
            @Param("status") StatusPedido status);

    /** Pedidos dos clientes finalizados desde o instante, com um dos status, para o ranking (RN-CLI-007). */
    @Query("select p.id as id, p.clienteId as clienteId, p.realizadoEm as realizadoEm, p.total as total "
            + "from PedidoJpa p where p.clienteId in :clientes and p.status in :status and p.realizadoEm >= :desde")
    List<PedidoDoCliente> buscarDosClientesDesde(@Param("clientes") Collection<Long> clienteIds,
            @Param("status") Collection<StatusPedido> status, @Param("desde") LocalDateTime desde);

    interface PedidoDoCliente {

        Long getId();

        Long getClienteId();

        LocalDateTime getRealizadoEm();

        BigDecimal getTotal();
    }
}
