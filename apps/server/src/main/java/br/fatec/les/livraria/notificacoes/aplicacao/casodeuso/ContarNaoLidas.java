package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Conta as notificações não lidas do usuário para o contador do cabeçalho (DEC-13). */
@Component
@RequiredArgsConstructor
public class ContarNaoLidas {

    private final NotificacaoRepositorio notificacoes;

    /**
     * @param usuarioId destinatário; obrigatório
     */
    @Transactional(readOnly = true)
    public long executar(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        return notificacoes.contarNaoLidas(usuarioId);
    }
}
