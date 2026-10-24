package br.fatec.les.livraria.notificacoes.aplicacao.porta;

import java.util.ArrayList;
import java.util.List;

import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;

/** Porta de eventos em memória: registra o que seria entregue pelo SSE (DEC-13). */
public class EventosDeNotificacaoFake implements EventosDeNotificacao {

    private final List<Notificacao> publicadas = new ArrayList<>();

    @Override
    public void publicar(Notificacao notificacao) {
        publicadas.add(notificacao);
    }

    public List<Notificacao> publicadas() {
        return List.copyOf(publicadas);
    }
}
