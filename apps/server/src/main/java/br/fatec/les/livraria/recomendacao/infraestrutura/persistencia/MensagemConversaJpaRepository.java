package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;

public interface MensagemConversaJpaRepository extends JpaRepository<MensagemConversaJpa, Long> {

    @EntityGraph(attributePaths = {"livros", "chamadaIa"})
    List<MensagemConversaJpa> findByConversaIdOrderByEnviadaEmAscIdAsc(Long conversaId);

    /** Sem grafo de entidades: com coleção no fetch, o Hibernate aplicaria o limite em memória. */
    List<MensagemConversaJpa> findByConversaIdOrderByEnviadaEmDescIdDesc(Long conversaId, Limit limite);

    @Query("select count(m) from MensagemConversaJpa m where m.conversa.clienteId = :clienteId "
            + "and m.papel = :papel and m.enviadaEm > :desde")
    long contarDoCliente(@Param("clienteId") Long clienteId, @Param("papel") PapelDaMensagem papel,
            @Param("desde") LocalDateTime desde);

    @EntityGraph(attributePaths = {"livros", "chamadaIa"})
    @Query("select m from MensagemConversaJpa m where m.id = :id and m.conversa.clienteId = :clienteId "
            + "and m.conversa.arquivadaEm is null")
    Optional<MensagemConversaJpa> buscarNaConversaAtiva(@Param("id") Long id, @Param("clienteId") Long clienteId);
}
