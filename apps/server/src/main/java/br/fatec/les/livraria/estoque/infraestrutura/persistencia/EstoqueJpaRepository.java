package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EstoqueJpaRepository extends JpaRepository<EstoqueJpa, Long> {

    Optional<EstoqueJpa> findByLivroId(Long livroId);

    /** Trava a linha do saldo para reservar ou dar baixa sem vender além do estoque (DEC-15.3). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EstoqueJpa e where e.livroId = :livroId")
    Optional<EstoqueJpa> buscarPorLivroIdComLock(@Param("livroId") Long livroId);

    /**
     * Quantidade em mãos e soma das reservas ativas de vários livros em uma única consulta (DEC-15.3, RN-EST-006). O
     * {@code left join} mantém o livro sem reserva ativa no resultado, com zero reservado.
     */
    @Query(
            """
            select e.livroId as livroId,
                   e.quantidade as quantidade,
                   cast(coalesce(sum(r.quantidade), 0) as integer) as reservada
            from EstoqueJpa e
            left join ReservaJpa r on r.livroId = e.livroId and r.expiraEm > :agora
            where e.livroId in :livroIds
            group by e.livroId, e.quantidade
            """)
    List<SaldoDoLivroProjecao> buscarSaldos(
            @Param("livroIds") Collection<Long> livroIds, @Param("agora") LocalDateTime agora);
}
