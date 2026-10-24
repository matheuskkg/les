package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface EstoqueJpaRepository extends JpaRepository<EstoqueJpa, Long> {

    Optional<EstoqueJpa> findByLivroId(Long livroId);

    /** Trava a linha do saldo para reservar ou dar baixa sem vender além do estoque (DEC-15.3). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EstoqueJpa e where e.livroId = :livroId")
    Optional<EstoqueJpa> buscarPorLivroIdComLock(@Param("livroId") Long livroId);

    /** Quantidade em mãos de vários livros em uma única consulta (DEC-15.3). */
    @Query("""
            select e.livroId as livroId, e.quantidade as quantidade
            from EstoqueJpa e
            where e.livroId in :livroIds
            """)
    List<QuantidadeDoLivroProjecao> buscarQuantidades(@Param("livroIds") Collection<Long> livroIds);
}
