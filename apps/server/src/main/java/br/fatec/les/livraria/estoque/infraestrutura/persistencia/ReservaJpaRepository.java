package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaJpaRepository extends JpaRepository<ReservaJpa, Long> {

    /** Reserva de um livro para um dono; o item é único por livro no carrinho e no checkout (DEC-16). */
    Optional<ReservaJpa> findByLivroIdAndOrigemAndOrigemId(Long livroId, OrigemReserva origem, Long origemId);

    /** Reservas de um carrinho ou checkout, para renovar o prazo de todas (DEC-05.1). */
    List<ReservaJpa> findByOrigemAndOrigemId(OrigemReserva origem, Long origemId);

    /**
     * Reservas de um carrinho ou checkout cujo prazo ainda não venceu no instante informado. Pré-filtro de leitura, no
     * padrão de {@link #somarReservasAtivasExceto}: a definição de reserva ativa continua no domínio ({@code
     * Reserva.ativaEm}), aplicada por quem consulta (RN-VEN-002, RNF-GER-001).
     */
    List<ReservaJpa> findByOrigemAndOrigemIdAndExpiraEmAfter(
            OrigemReserva origem, Long origemId, LocalDateTime expiraEmApos);

    /**
     * Reservas ativas do livro, desconsiderando a reserva do próprio dono (RN-VEN-001). Reserva vencida não conta como
     * ativa, porque só o prazo no futuro consome o disponível (DEC-16).
     */
    @Query(
            """
            select cast(coalesce(sum(r.quantidade), 0) as integer)
            from ReservaJpa r
            where r.livroId = :livroId
              and r.expiraEm > :agora
              and r.id <> :reservaExcluida
            """)
    int somarReservasAtivasExceto(
            @Param("livroId") Long livroId,
            @Param("agora") LocalDateTime agora,
            @Param("reservaExcluida") Long reservaExcluida);
}
