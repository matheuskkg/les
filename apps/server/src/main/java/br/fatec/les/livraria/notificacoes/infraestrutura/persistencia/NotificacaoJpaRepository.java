package br.fatec.les.livraria.notificacoes.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas da tabela {@code notificacao}, sempre por usuário e cobertas pelos índices da entidade (DEC-13). */
public interface NotificacaoJpaRepository extends JpaRepository<NotificacaoJpa, Long> {

    /** Histórico paginado do usuário; a ordenação vem do {@code Pageable} montado a partir da {@code Paginacao}. */
    Page<NotificacaoJpa> findByUsuarioId(Long usuarioId, Pageable paginacao);

    /** Não lidas do usuário, para o contador do cabeçalho (DEC-13). */
    long countByUsuarioIdAndLidaEmIsNull(Long usuarioId);

    /** Não lidas do usuário, carregadas para que a leitura passe pela auditoria do Envers (RNF-GER-002). */
    List<NotificacaoJpa> findByUsuarioIdAndLidaEmIsNull(Long usuarioId);

    /**
     * Até 100 notificações posteriores ao {@code Last-Event-ID}, em ordem crescente de id (DEC-13). O limite evita
     * carregar o histórico inteiro numa reconexão antiga; o que passar dele continua na listagem.
     */
    List<NotificacaoJpa> findTop100ByUsuarioIdAndIdGreaterThanOrderByIdAsc(Long usuarioId, Long ultimoId);
}
