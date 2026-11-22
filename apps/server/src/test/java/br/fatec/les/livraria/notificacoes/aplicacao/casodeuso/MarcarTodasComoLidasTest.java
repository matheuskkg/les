package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorioFake;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/** Leitura de todas as notificações do próprio usuário (DEC-13). */
class MarcarTodasComoLidasTest {

    private static final Long CLIENTE = 42L;
    private static final Long OUTRO_CLIENTE = 43L;
    private static final LocalDateTime CRIADA_EM = LocalDateTime.of(2026, 10, 24, 8, 0);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 9, 0);

    @Test
    void dec13_marcaSomenteAsNaoLidasDoUsuarioNoInstanteDoRelogio() {
        NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
        Notificacao primeira = criar(notificacoes, CLIENTE);
        Notificacao segunda = criar(notificacoes, CLIENTE);
        Notificacao deOutro = criar(notificacoes, OUTRO_CLIENTE);
        MarcarTodasComoLidas caso = new MarcarTodasComoLidas(notificacoes, () -> AGORA);

        int marcadas = caso.executar(CLIENTE);

        assertThat(marcadas).isEqualTo(2);
        assertThat(notificacoes.contarNaoLidas(CLIENTE)).isZero();
        assertThat(notificacoes.contarNaoLidas(OUTRO_CLIENTE)).isEqualTo(1);
        assertThat(primeira.getLidaEm()).isEqualTo(AGORA);
        assertThat(segunda.getLidaEm()).isEqualTo(AGORA);
        assertThat(deOutro.getLidaEm()).isNull();
    }

    @Test
    void dec13_semNaoLidasNaoAlteraNada() {
        NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
        MarcarTodasComoLidas caso = new MarcarTodasComoLidas(notificacoes, () -> AGORA);

        assertThat(caso.executar(CLIENTE)).isZero();
    }

    private static Notificacao criar(NotificacaoRepositorioFake notificacoes, Long usuarioId) {
        return notificacoes.salvar(
                Notificacao.nova(
                        usuarioId, TipoNotificacao.PEDIDO_ENTREGUE, "Pedido entregue.", null, null, CRIADA_EM));
    }
}
