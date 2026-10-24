package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import lombok.RequiredArgsConstructor;

/**
 * Marca uma notificação do próprio usuário como lida (DEC-13). Notificação inexistente ou de outro usuário responde
 * 404, como nas demais rotas {@code /api/me} (DEC-07); a segunda leitura não altera o instante original.
 */
@Component
@RequiredArgsConstructor
public class MarcarComoLida {

    private final NotificacaoRepositorio notificacoes;
    private final Relogio relogio;

    /**
     * @param usuarioId      dono da notificação, obtido da sessão
     * @param notificacaoId  id informado na rota
     * @throws ExcecaoDeRecursoNaoEncontrado se a notificação não existir ou for de outro usuário
     */
    @Transactional
    public void executar(Long usuarioId, Long notificacaoId) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(notificacaoId, "notificacaoId");
        Notificacao notificacao = notificacoes.buscarPorId(notificacaoId)
                .filter(encontrada -> encontrada.pertenceA(usuarioId))
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Notificação não encontrada."));
        if (notificacao.lida()) {
            return;
        }
        notificacao.marcarComoLida(relogio.agora());
        notificacoes.salvar(notificacao);
    }
}
