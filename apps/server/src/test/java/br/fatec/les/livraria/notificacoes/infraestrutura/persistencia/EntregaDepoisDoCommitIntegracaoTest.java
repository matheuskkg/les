package br.fatec.les.livraria.notificacoes.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.EventosDeNotificacao;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Envio depois do commit com transações reais no H2 (DEC-13). A notificação só chega ao SSE quando a transação de quem
 * chamou faz commit; desfeita a transação, nada é persistido nem entregue.
 */
@SpringBootTest
class EntregaDepoisDoCommitIntegracaoTest {

    private static final String SENHA = "Senha@123";

    @TestConfiguration
    static class Configuracao {

        /** Substitui o registro SSE por um gravador para observar a entrega sem abrir conexão HTTP. */
        @Bean
        @Primary
        EventosGravados eventosGravados() {
            return new EventosGravados();
        }
    }

    static class EventosGravados implements EventosDeNotificacao {

        private final List<Notificacao> publicadas = new ArrayList<>();

        @Override
        public void publicar(Notificacao notificacao) {
            publicadas.add(notificacao);
        }

        List<Notificacao> publicadas() {
            return List.copyOf(publicadas);
        }

        void limpar() {
            publicadas.clear();
        }
    }

    @Autowired private TransactionTemplate transacoes;

    @Autowired private Notificar notificar;

    @Autowired private NotificacaoRepositorio notificacoes;

    @Autowired private UsuarioRepositorio usuarios;

    @Autowired private CodificadorDeSenha senhas;

    @Autowired private EventosGravados eventos;

    @BeforeEach
    void limparEventos() {
        eventos.limpar();
    }

    @Test
    void dec13_naoPersisteNemEntregaQuandoATransacaoEhDesfeita() {
        Long cliente = criarCliente("entrega.rollback@livraria.test");

        transacoes.executeWithoutResult(status -> {
            notificar.executar(
                    Notificar.Comando.sobre(
                            cliente, TipoNotificacao.PEDIDO_APROVADO, "Pedido aprovado.", "pedido", 10042L));
            assertThat(notificacoes.contarNaoLidas(cliente)).isEqualTo(1);
            status.setRollbackOnly();
        });

        assertThat(eventos.publicadas()).isEmpty();
        assertThat(notificacoes.contarNaoLidas(cliente)).isZero();
    }

    @Test
    void dec13_entregaSomenteDepoisDoCommit() {
        Long cliente = criarCliente("entrega.commit@livraria.test");
        List<Long> idsDuranteATransacao = new ArrayList<>();

        transacoes.executeWithoutResult(status -> {
            Notificacao notificacao =
                    notificar.executar(
                            Notificar.Comando.semReferencia(
                                    cliente, TipoNotificacao.PEDIDO_REPROVADO, "Pedido reprovado."));
            idsDuranteATransacao.add(notificacao.getId());
            assertThat(eventos.publicadas()).isEmpty();
        });

        assertThat(eventos.publicadas()).extracting(Notificacao::getId).containsExactlyElementsOf(idsDuranteATransacao);
        assertThat(notificacoes.contarNaoLidas(cliente)).isEqualTo(1);
    }

    @Test
    void dec13_entregaAposOCommitDeUmaTransacaoExternaQueParticipa() {
        Long cliente = criarCliente("entrega.participa@livraria.test");

        transacoes.executeWithoutResult(status -> {
            notificar.executar(
                    Notificar.Comando.semReferencia(cliente, TipoNotificacao.TROCA_CONCLUIDA, "Troca concluída."));
            notificar.executar(
                    Notificar.Comando.sobre(
                            cliente, TipoNotificacao.TROCA_AUTORIZADA, "Troca autorizada.", "troca", 10042L));
            assertThat(eventos.publicadas()).isEmpty();
        });

        assertThat(eventos.publicadas()).hasSize(2);
    }

    @Test
    void dec13_entregaImediataForaDeUmaTransacao() {
        Long cliente = criarCliente("entrega.sem.transacao@livraria.test");

        notificar.executar(
                Notificar.Comando.semReferencia(cliente, TipoNotificacao.PEDIDO_DESPACHADO, "Pedido despachado."));

        assertThat(eventos.publicadas()).hasSize(1);
    }

    private Long criarCliente(String email) {
        Usuario cliente = usuarios.salvar(new Usuario(null, email, null, senhas.codificar(SENHA), Papel.CLIENTE, null));
        return cliente.getId();
    }
}
