package br.fatec.les.livraria.notificacoes.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Regras do modelo de notificação (DEC-13, DEC-16, RN-VEN-014, RN-VEN-016). */
class NotificacaoTest {

    private static final Long CLIENTE = 42L;
    private static final LocalDateTime CRIADA_EM = LocalDateTime.of(2026, 10, 24, 10, 0);
    private static final LocalDateTime LIDA_EM = LocalDateTime.of(2026, 10, 24, 11, 0);

    @Test
    void dec13_notificacaoNovaNasceNaoLidaESemReferencia() {
        Notificacao notificacao = Notificacao.nova(CLIENTE, TipoNotificacao.PEDIDO_APROVADO,
                "Seu pedido foi aprovado.", null, null, CRIADA_EM);

        assertThat(notificacao.getId()).isNull();
        assertThat(notificacao.lida()).isFalse();
        assertThat(notificacao.getLidaEm()).isNull();
        assertThat(notificacao.getCriadaEm()).isEqualTo(CRIADA_EM);
        assertThat(notificacao.getRecurso()).isNull();
        assertThat(notificacao.getRecursoId()).isNull();
    }

    @Test
    void dec13_guardaATipoMensagemEOIdDoRecurso() {
        Notificacao notificacao = Notificacao.nova(CLIENTE, TipoNotificacao.TROCA_AUTORIZADA,
                "Sua troca foi autorizada.", "troca", 10042L, CRIADA_EM);

        assertThat(notificacao.getTipo()).isEqualTo(TipoNotificacao.TROCA_AUTORIZADA);
        assertThat(notificacao.getMensagem()).isEqualTo("Sua troca foi autorizada.");
        assertThat(notificacao.getRecurso()).isEqualTo("troca");
        assertThat(notificacao.getRecursoId()).isEqualTo(10042L);
    }

    @Test
    void dec13_referenciaExigeRecursoEIdJuntos() {
        assertThatThrownBy(() -> Notificacao.nova(CLIENTE, TipoNotificacao.PEDIDO_APROVADO,
                "Aprovado.", "pedido", null, CRIADA_EM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recurso e o id juntos");

        assertThatThrownBy(() -> Notificacao.nova(CLIENTE, TipoNotificacao.PEDIDO_APROVADO,
                "Aprovado.", null, 10042L, CRIADA_EM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recurso e o id juntos");
    }

    @Test
    void dec13_mensagemEhObrigatoriaELimitadaPeloMer() {
        assertThatThrownBy(() -> Notificacao.nova(CLIENTE, TipoNotificacao.PEDIDO_APROVADO,
                "  ", null, null, CRIADA_EM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obrigatória");

        String longa = "a".repeat(Notificacao.TAMANHO_MAXIMO_DA_MENSAGEM + 1);
        assertThatThrownBy(() -> Notificacao.nova(CLIENTE, TipoNotificacao.PEDIDO_APROVADO,
                longa, null, null, CRIADA_EM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no máximo");
    }

    @Test
    void dec13_marcarComoLidaGuardaOInstanteE_NaoMudaNaSegundaVezParaORnVen016() {
        Notificacao notificacao = Notificacao.nova(CLIENTE, TipoNotificacao.TROCA_AUTORIZADA,
                "Sua troca foi autorizada.", "troca", 10042L, CRIADA_EM);

        notificacao.marcarComoLida(LIDA_EM);
        notificacao.marcarComoLida(LIDA_EM.plusHours(1));

        assertThat(notificacao.lida()).isTrue();
        assertThat(notificacao.getLidaEm()).isEqualTo(LIDA_EM);
    }

    @Test
    void dec07_notificacaoPertenceSomenteAoProprioUsuario() {
        Notificacao notificacao = Notificacao.nova(CLIENTE, TipoNotificacao.PEDIDO_ENTREGUE,
                "Pedido entregue.", null, null, CRIADA_EM);

        assertThat(notificacao.pertenceA(CLIENTE)).isTrue();
        assertThat(notificacao.pertenceA(43L)).isFalse();
        assertThat(notificacao.pertenceA(null)).isFalse();
    }

    @Test
    void dec13_usuarioETipoSaoObrigatorios() {
        assertThatThrownBy(() -> Notificacao.nova(null, TipoNotificacao.PEDIDO_APROVADO,
                "Aprovado.", null, null, CRIADA_EM))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Notificacao.nova(CLIENTE, null, "Aprovado.", null, null, CRIADA_EM))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void dec13_tipoDeAdminEhMarcadoComoDestinatarioAdmin() {
        assertThat(TipoNotificacao.TROCA_SOLICITADA.destinatario())
                .isEqualTo(TipoNotificacao.Destinatario.ADMIN);
        assertThat(TipoNotificacao.PEDIDO_AGUARDANDO_DESPACHO.destinatario())
                .isEqualTo(TipoNotificacao.Destinatario.ADMIN);
        assertThat(TipoNotificacao.TROCA_AUTORIZADA.destinatario())
                .isEqualTo(TipoNotificacao.Destinatario.CLIENTE);
    }
}
