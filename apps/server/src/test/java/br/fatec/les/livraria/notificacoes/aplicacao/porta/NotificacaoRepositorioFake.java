package br.fatec.les.livraria.notificacoes.aplicacao.porta;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositório em memória dos testes de caso de uso (DEC-13). */
public class NotificacaoRepositorioFake implements NotificacaoRepositorio {

    private final Map<Long, Notificacao> porId = new HashMap<>();
    private final List<Paginacao> paginacoes = new ArrayList<>();
    private long proximoId = 10000L;

    @Override
    public Notificacao salvar(Notificacao notificacao) {
        Notificacao guardada = notificacao.getId() == null ? copiar(notificacao, proximoId++) : notificacao;
        porId.put(guardada.getId(), guardada);
        return guardada;
    }

    @Override
    public Optional<Notificacao> buscarPorId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public Pagina<Notificacao> listar(Long usuarioId, Paginacao paginacao) {
        paginacoes.add(paginacao);
        List<Notificacao> doUsuario =
                porId.values().stream().filter(notificacao -> notificacao.pertenceA(usuarioId)).toList();
        return new Pagina<>(doUsuario, paginacao.pagina(), paginacao.tamanho(), doUsuario.size(), 1);
    }

    @Override
    public long contarNaoLidas(Long usuarioId) {
        return porId.values().stream()
                .filter(notificacao -> notificacao.pertenceA(usuarioId) && !notificacao.lida())
                .count();
    }

    @Override
    public int marcarTodasComoLidas(Long usuarioId, LocalDateTime agora) {
        List<Notificacao> naoLidas =
                porId.values().stream()
                        .filter(notificacao -> notificacao.pertenceA(usuarioId) && !notificacao.lida())
                        .toList();
        naoLidas.forEach(notificacao -> notificacao.marcarComoLida(agora));
        return naoLidas.size();
    }

    @Override
    public List<Notificacao> listarComIdMaiorQue(Long usuarioId, Long ultimoId) {
        return porId.values().stream()
                .filter(notificacao -> notificacao.pertenceA(usuarioId))
                .filter(notificacao -> notificacao.getId() > ultimoId)
                .sorted((primeira, segunda) -> Long.compare(primeira.getId(), segunda.getId()))
                .toList();
    }

    /** Última paginação recebida em {@link #listar}, para conferir a ordem padrão aplicada pelo caso de uso. */
    public Paginacao ultimaPaginacao() {
        return paginacoes.get(paginacoes.size() - 1);
    }

    private static Notificacao copiar(Notificacao notificacao, long id) {
        return new Notificacao(
                id,
                notificacao.getUsuarioId(),
                notificacao.getTipo(),
                notificacao.getMensagem(),
                notificacao.getRecurso(),
                notificacao.getRecursoId(),
                notificacao.getCriadaEm(),
                notificacao.getLidaEm());
    }
}
