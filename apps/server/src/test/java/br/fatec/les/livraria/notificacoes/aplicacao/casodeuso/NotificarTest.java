package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.notificacoes.aplicacao.EntregaDepoisDoCommit;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.EventosDeNotificacaoFake;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorioFake;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

/** Notificação de um usuário, com as portas em memória (DEC-13, RN-VEN-014, RN-VEN-016). */
class NotificarTest {

    private static final Long CLIENTE = 42L;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 9, 0);

    private final NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
    private final EventosDeNotificacaoFake eventos = new EventosDeNotificacaoFake();
    private final Notificar notificar = new Notificar(notificacoes,
            new EntregaDepoisDoCommit(eventos), () -> AGORA);

    @Test
    void rnVen014_persisteATipoMensagemEOInstanteDoRelogio() {
        Notificacao notificacao = notificar.executar(Notificar.Comando.semReferencia(CLIENTE,
                TipoNotificacao.CARRINHO_EXPIRANDO, "Seu carrinho expira em 5 minutos."));

        assertThat(notificacao.getId()).isNotNull();
        assertThat(notificacao.getUsuarioId()).isEqualTo(CLIENTE);
        assertThat(notificacao.getTipo()).isEqualTo(TipoNotificacao.CARRINHO_EXPIRANDO);
        assertThat(notificacao.getCriadaEm()).isEqualTo(AGORA);
        assertThat(notificacao.lida()).isFalse();
        assertThat(notificacoes.contarNaoLidas(CLIENTE)).isEqualTo(1);
    }

    @Test
    void rnVen016_guardaAReferenciaDaTrocaQueOriginaANotificacao() {
        Notificacao notificacao = notificar.executar(Notificar.Comando.sobre(CLIENTE,
                TipoNotificacao.TROCA_AUTORIZADA, "Sua troca foi autorizada.", "troca", 10042L));

        assertThat(notificacao.getRecurso()).isEqualTo("troca");
        assertThat(notificacao.getRecursoId()).isEqualTo(10042L);
    }

    @Test
    void dec13_entregaPorSseDepoisDePersistir() {
        Notificacao notificacao = notificar.executar(Notificar.Comando.semReferencia(CLIENTE,
                TipoNotificacao.PEDIDO_APROVADO, "Seu pedido foi aprovado."));

        assertThat(eventos.publicadas()).containsExactly(notificacao);
    }

    @Test
    void dec13_naoPersisteNemEntregaQuandoAReferenciaEhInvalida() {
        Throwable erro = catchThrowable(() -> notificar.executar(new Notificar.Comando(CLIENTE,
                TipoNotificacao.PEDIDO_APROVADO, "Aprovado.", "pedido", null)));

        assertThat(erro).isInstanceOf(IllegalArgumentException.class);
        assertThat(eventos.publicadas()).isEmpty();
        assertThat(notificacoes.contarNaoLidas(CLIENTE)).isZero();
    }

    @Test
    void dec13_oComandoSobreMontaAReferenciaCompleta() {
        Notificar.Comando comando = Notificar.Comando.sobre(CLIENTE, TipoNotificacao.CHECKOUT_EXPIRADO,
                "Seu checkout expirou.", "checkout", 77L);

        assertThat(comando.recurso()).isEqualTo("checkout");
        assertThat(comando.recursoId()).isEqualTo(77L);
        assertThat(Notificar.Comando.semReferencia(CLIENTE, TipoNotificacao.CHECKOUT_EXPIRANDO, "Aviso").recurso())
                .isNull();
    }
}
