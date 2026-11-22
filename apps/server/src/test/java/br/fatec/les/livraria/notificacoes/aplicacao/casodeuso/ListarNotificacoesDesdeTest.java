package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorioFake;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/** Reenvio das notificações posteriores ao {@code Last-Event-ID} (DEC-13). */
class ListarNotificacoesDesdeTest {

    private static final Long CLIENTE = 42L;
    private static final Long OUTRO_CLIENTE = 43L;

    private final NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
    private final ListarNotificacoesDesde caso = new ListarNotificacoesDesde(notificacoes);

    @Test
    void dec13_trazSomenteAsNotificacoesDoUsuarioComIdMaior() {
        Notificacao primeira = criar(CLIENTE);
        Notificacao segunda = criar(CLIENTE);
        criar(OUTRO_CLIENTE);

        assertThat(caso.executar(CLIENTE, primeira.getId()))
                .extracting(Notificacao::getId)
                .containsExactly(segunda.getId());
    }

    @Test
    void dec13_ordenaPeloIdCrescente() {
        Notificacao primeira = criar(CLIENTE);
        Notificacao segunda = criar(CLIENTE);
        Notificacao terceira = criar(CLIENTE);

        assertThat(caso.executar(CLIENTE, 0L))
                .extracting(Notificacao::getId)
                .containsExactly(primeira.getId(), segunda.getId(), terceira.getId());
    }

    @Test
    void dec13_semNotificacaoPosteriorDevolveListaVazia() {
        Notificacao ultima = criar(CLIENTE);

        assertThat(caso.executar(CLIENTE, ultima.getId())).isEmpty();
    }

    private Notificacao criar(Long usuarioId) {
        return notificacoes.salvar(
                Notificacao.nova(
                        usuarioId,
                        TipoNotificacao.PEDIDO_APROVADO,
                        "Pedido aprovado.",
                        null,
                        null,
                        LocalDateTime.of(2026, 10, 24, 8, 0)));
    }
}
