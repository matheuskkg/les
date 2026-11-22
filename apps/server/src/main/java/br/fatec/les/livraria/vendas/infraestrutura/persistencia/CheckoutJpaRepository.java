package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.vendas.dominio.modelo.SituacaoCheckout;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckoutJpaRepository extends JpaRepository<CheckoutJpa, Long> {

    /** Checkout do cliente na situação informada, com os itens numa consulta só. */
    @EntityGraph(attributePaths = "itens")
    Optional<CheckoutJpa> findFirstByClienteIdAndSituacao(Long clienteId, SituacaoCheckout situacao);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CheckoutJpa c where c.clienteId = :clienteId and c.situacao = :situacao")
    Optional<CheckoutJpa> buscarPorClienteComLock(
            @Param("clienteId") Long clienteId, @Param("situacao") SituacaoCheckout situacao);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CheckoutJpa c where c.id = :id")
    Optional<CheckoutJpa> buscarPorIdComLock(@Param("id") Long id);

    @Query("select c.clienteId from CheckoutJpa c where c.id = :id")
    Optional<Long> buscarClienteId(@Param("id") Long id);

    /** Checkouts abertos com o prazo vencido (RN-VEN-014). */
    @Query(
            """
            select c.id from CheckoutJpa c
            where c.situacao = :situacao and c.expiraEm <= :agora
            order by c.expiraEm
            """)
    List<Long> buscarIdsVencidos(@Param("situacao") SituacaoCheckout situacao, @Param("agora") LocalDateTime agora);

    /** Checkouts abertos sem aviso cujo prazo vence em {@code (agora, limite]} (RN-VEN-014). */
    @Query(
            """
            select c.id from CheckoutJpa c
            where c.situacao = :situacao and c.avisoEnviadoEm is null
              and c.expiraEm > :agora and c.expiraEm <= :limite
            order by c.expiraEm
            """)
    List<Long> buscarIdsParaAvisar(
            @Param("situacao") SituacaoCheckout situacao,
            @Param("agora") LocalDateTime agora,
            @Param("limite") LocalDateTime limite);
}
