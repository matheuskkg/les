package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Marca como lidas todas as notificações não lidas do próprio usuário (DEC-13). */
@Component
@RequiredArgsConstructor
public class MarcarTodasComoLidas {

    private final NotificacaoRepositorio notificacoes;
    private final Relogio relogio;

    /**
     * @param usuarioId dono das notificações, obtido da sessão
     * @return quantas notificações foram marcadas
     */
    @Transactional
    public int executar(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        return notificacoes.marcarTodasComoLidas(usuarioId, relogio.agora());
    }
}
