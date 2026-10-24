package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorioFake;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

/** Leitura de uma notificação do próprio usuário (DEC-07, DEC-13). */
class MarcarComoLidaTest {

    private static final Long CLIENTE = 42L;
    private static final Long OUTRO_CLIENTE = 43L;
    private static final LocalDateTime CRIADA_EM = LocalDateTime.of(2026, 10, 24, 8, 0);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 9, 0);

    private final NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
    private final MarcarComoLida caso = new MarcarComoLida(notificacoes, () -> AGORA);

    @Test
    void dec13_marcaComoLidaANotificacaoDoProprioUsuario() {
        Notificacao notificacao = criar(CLIENTE);

        caso.executar(CLIENTE, notificacao.getId());

        assertThat(notificacoes.buscarPorId(notificacao.getId()))
                .get()
                .extracting(Notificacao::getLidaEm)
                .isEqualTo(AGORA);
    }

    @Test
    void dec07_notificacaoDeOutroUsuarioResponde404() {
        Notificacao notificacao = criar(OUTRO_CLIENTE);

        assertThatThrownBy(() -> caso.executar(CLIENTE, notificacao.getId()))
                .isInstanceOf(ExcecaoDeRecursoNaoEncontrado.class)
                .hasMessage("Notificação não encontrada.");
        assertThat(notificacoes.buscarPorId(notificacao.getId()))
                .get()
                .extracting(Notificacao::getLidaEm)
                .isNull();
    }

    @Test
    void dec07_notificacaoInexistenteResponde404() {
        assertThatThrownBy(() -> caso.executar(CLIENTE, 999_999L))
                .isInstanceOf(ExcecaoDeRecursoNaoEncontrado.class)
                .hasMessage("Notificação não encontrada.");
    }

    @Test
    void dec13_segundaLeituraPreservaOInstanteOriginal() {
        Notificacao notificacao = criar(CLIENTE);
        caso.executar(CLIENTE, notificacao.getId());

        MarcarComoLida depois = new MarcarComoLida(notificacoes, () -> AGORA.plusHours(2));
        depois.executar(CLIENTE, notificacao.getId());

        assertThat(notificacoes.buscarPorId(notificacao.getId()))
                .get()
                .extracting(Notificacao::getLidaEm)
                .isEqualTo(AGORA);
    }

    private Notificacao criar(Long usuarioId) {
        return notificacoes.salvar(Notificacao.nova(usuarioId, TipoNotificacao.PEDIDO_APROVADO,
                "Seu pedido foi aprovado.", "pedido", 10042L, CRIADA_EM));
    }
}
