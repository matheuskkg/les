package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.Collection;
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

    /**
     * Ids das últimas mensagens da conversa, com o limite aplicado no banco. Depois as mensagens são buscadas de uma
     * vez com os livros citados ({@link #buscarPorIds}): com coleção no fetch, o Hibernate aplicaria o limite em
     * memória, e sem o fetch cada mensagem carregaria os livros numa consulta própria (RNF-GER-001, RNF-REC-001).
     */
    @Query("select m.id from MensagemConversaJpa m where m.conversa.id = :conversaId "
            + "order by m.enviadaEm desc, m.id desc")
    List<Long> idsDasUltimas(@Param("conversaId") Long conversaId, Limit limite);

    /** Mensagens pelos ids, com os livros citados e a chamada associada, em uma única consulta (RNF-GER-001). */
    @EntityGraph(attributePaths = {"livros", "chamadaIa"})
    @Query("select m from MensagemConversaJpa m where m.id in :ids")
    List<MensagemConversaJpa> buscarPorIds(@Param("ids") Collection<Long> ids);

    @Query("select count(m) from MensagemConversaJpa m where m.conversa.clienteId = :clienteId "
            + "and m.papel = :papel and m.enviadaEm > :desde")
    long contarDoCliente(@Param("clienteId") Long clienteId, @Param("papel") PapelDaMensagem papel,
            @Param("desde") LocalDateTime desde);

    @EntityGraph(attributePaths = {"livros", "chamadaIa"})
    @Query("select m from MensagemConversaJpa m where m.id = :id and m.conversa.clienteId = :clienteId "
            + "and m.conversa.arquivadaEm is null")
    Optional<MensagemConversaJpa> buscarNaConversaAtiva(@Param("id") Long id, @Param("clienteId") Long clienteId);
}
