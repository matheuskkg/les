package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CarrinhoJpaRepository extends JpaRepository<CarrinhoJpa, Long> {

    /** Carrinho do cliente com os itens, numa consulta só. */
    @EntityGraph(attributePaths = "itens")
    Optional<CarrinhoJpa> findByClienteId(Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CarrinhoJpa c where c.clienteId = :clienteId")
    Optional<CarrinhoJpa> buscarPorClienteComLock(@Param("clienteId") Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CarrinhoJpa c where c.id = :id")
    Optional<CarrinhoJpa> buscarPorIdComLock(@Param("id") Long id);

    /** Carrinhos vencidos para a tarefa de expiração (RN-VEN-014). Carrinho vazio não tem prazo. */
    @Query("select c.id from CarrinhoJpa c where c.expiraEm <= :agora order by c.expiraEm")
    List<Long> buscarIdsVencidos(@Param("agora") LocalDateTime agora);

    /** Carrinhos sem aviso cujo prazo vence em {@code (agora, limite]} (RN-VEN-014). */
    @Query("""
            select c.id from CarrinhoJpa c
            where c.avisoEnviadoEm is null and c.expiraEm > :agora and c.expiraEm <= :limite
            order by c.expiraEm
            """)
    List<Long> buscarIdsParaAvisar(@Param("agora") LocalDateTime agora, @Param("limite") LocalDateTime limite);
}
