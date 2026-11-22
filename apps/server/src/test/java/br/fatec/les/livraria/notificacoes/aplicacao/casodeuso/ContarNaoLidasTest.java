package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorioFake;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/** Contador de não lidas do cabeçalho (DEC-13). */
class ContarNaoLidasTest {

    private static final Long CLIENTE = 42L;
    private static final Long OUTRO_CLIENTE = 43L;

    @Test
    void dec13_contaSomenteAsNaoLidasDoUsuario() {
        NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
        criar(notificacoes, CLIENTE);
        criar(notificacoes, CLIENTE);
        Notificacao lida = criar(notificacoes, CLIENTE);
        lida.marcarComoLida(LocalDateTime.of(2026, 10, 24, 9, 0));
        criar(notificacoes, OUTRO_CLIENTE);

        assertThat(new ContarNaoLidas(notificacoes).executar(CLIENTE)).isEqualTo(2);
        assertThat(new ContarNaoLidas(notificacoes).executar(OUTRO_CLIENTE)).isEqualTo(1);
    }

    @Test
    void dec13_usuarioSemNotificacaoTemZero() {
        assertThat(new ContarNaoLidas(new NotificacaoRepositorioFake()).executar(CLIENTE)).isZero();
    }

    private static Notificacao criar(NotificacaoRepositorioFake notificacoes, Long usuarioId) {
        return notificacoes.salvar(
                Notificacao.nova(
                        usuarioId,
                        TipoNotificacao.PEDIDO_APROVADO,
                        "Pedido aprovado.",
                        "pedido",
                        10042L,
                        LocalDateTime.of(2026, 10, 24, 8, 0)));
    }
}
