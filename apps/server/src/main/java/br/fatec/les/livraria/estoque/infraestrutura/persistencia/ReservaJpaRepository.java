package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaJpaRepository extends JpaRepository<ReservaJpa, Long> {

    /**
     * Soma das reservas ainda válidas de vários livros em uma única agregação (DEC-15.3, RN-EST-006).
     * Reserva vencida, com {@code expiraEm} menor ou igual a {@code agora}, não entra na soma.
     */
    @Query("""
            select r.livroId as livroId, sum(r.quantidade) as quantidade
            from ReservaJpa r
            where r.livroId in :livroIds and r.expiraEm > :agora
            group by r.livroId
            """)
    List<ReservaAtivaDoLivroProjecao> somarReservasAtivas(@Param("livroIds") Collection<Long> livroIds,
            @Param("agora") LocalDateTime agora);
}
