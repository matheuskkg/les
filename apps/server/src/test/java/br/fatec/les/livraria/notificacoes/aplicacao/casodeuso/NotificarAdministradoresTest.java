package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ListarAdministradores;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorioFake;
import br.fatec.les.livraria.notificacoes.aplicacao.EntregaDepoisDoCommit;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.EventosDeNotificacaoFake;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorioFake;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

/** Notificação de todos os administradores (DEC-13, RN-VEN-016). */
class NotificarAdministradoresTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 9, 0);

    @Test
    void rnVen016_criaUmaNotificacaoParaCadaAdministrador() {
        NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
        EventosDeNotificacaoFake eventos = new EventosDeNotificacaoFake();
        NotificarAdministradores caso = caso(notificacoes, eventos, List.of(1L, 7L));

        List<Notificacao> criadas = caso.executar(NotificarAdministradores.Comando.sobre(
                TipoNotificacao.TROCA_SOLICITADA, "Nova troca para autorizar.", "troca", 10042L));

        assertThat(criadas).extracting(Notificacao::getUsuarioId).containsExactly(1L, 7L);
        assertThat(criadas).allSatisfy(notificacao -> {
            assertThat(notificacao.getTipo()).isEqualTo(TipoNotificacao.TROCA_SOLICITADA);
            assertThat(notificacao.getRecurso()).isEqualTo("troca");
            assertThat(notificacao.getRecursoId()).isEqualTo(10042L);
            assertThat(notificacao.getCriadaEm()).isEqualTo(AGORA);
        });
        assertThat(eventos.publicadas()).hasSize(2);
    }

    @Test
    void dec13_semAdministradorNaoCriaNemEntregaNada() {
        NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
        EventosDeNotificacaoFake eventos = new EventosDeNotificacaoFake();
        NotificarAdministradores caso = caso(notificacoes, eventos, List.of());

        List<Notificacao> criadas = caso.executar(NotificarAdministradores.Comando.semReferencia(
                TipoNotificacao.PEDIDO_AGUARDANDO_DESPACHO, "Pedido aguardando despacho."));

        assertThat(criadas).isEmpty();
        assertThat(eventos.publicadas()).isEmpty();
    }

    private static NotificarAdministradores caso(NotificacaoRepositorioFake notificacoes,
            EventosDeNotificacaoFake eventos, List<Long> administradores) {
        Notificar notificar = new Notificar(notificacoes, new EntregaDepoisDoCommit(eventos), () -> AGORA);
        return new NotificarAdministradores(
                new ListarAdministradores(new UsuarioRepositorioFake(administradores)), notificar);
    }

    @Test
    void dec13_tipoDestinadoAoClienteNaoVaiParaOsAdministradores() {
        assertThatThrownBy(() -> NotificarAdministradores.Comando.semReferencia(
                TipoNotificacao.PEDIDO_APROVADO, "Pedido aprovado."))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
