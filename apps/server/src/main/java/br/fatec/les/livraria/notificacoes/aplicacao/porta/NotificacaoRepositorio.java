package br.fatec.les.livraria.notificacoes.aplicacao.porta;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saída das notificações persistidas (DEC-13, DEC-16), que são a fonte da verdade do stream SSE. Todas as
 * consultas são por usuário: o cliente nunca informa o id de outra pessoa.
 */
public interface NotificacaoRepositorio {

    /** Persiste uma notificação nova ou atualiza a leitura de uma existente. */
    Notificacao salvar(Notificacao notificacao);

    Optional<Notificacao> buscarPorId(Long id);

    /**
     * Histórico do usuário, com as não lidas primeiro e as mais recentes antes (DEC-13).
     *
     * @param usuarioId destinatário
     * @param paginacao página, tamanho e ordenação já validados; sem ordenação, aplica a ordem padrão
     */
    Pagina<Notificacao> listar(Long usuarioId, Paginacao paginacao);

    /** Quantidade de não lidas do usuário, para o contador do cabeçalho (DEC-13). */
    long contarNaoLidas(Long usuarioId);

    /** Marca como lidas todas as notificações não lidas do usuário e devolve quantas foram alteradas. */
    int marcarTodasComoLidas(Long usuarioId, LocalDateTime agora);

    /**
     * Notificações do usuário com id maior que {@code ultimoId} (no máximo 100), em ordem crescente de id, para a
     * reconexão do {@code EventSource} com {@code Last-Event-ID} (DEC-13).
     */
    List<Notificacao> listarComIdMaiorQue(Long usuarioId, Long ultimoId);
}
