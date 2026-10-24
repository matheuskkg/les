package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorioFake;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

/** Listagem paginada do histórico (DEC-13, DEC-07). */
class ListarNotificacoesTest {

    private static final Long CLIENTE = 42L;
    private static final Long OUTRO_CLIENTE = 43L;
    private static final LocalDateTime CRIADA_EM = LocalDateTime.of(2026, 10, 24, 8, 0);

    private final NotificacaoRepositorioFake notificacoes = new NotificacaoRepositorioFake();
    private final ListarNotificacoes caso = new ListarNotificacoes(notificacoes);

    @Test
    void dec13_aplicaOrdemPadraoNaoLidasPrimeiroEMaisRecentesAntes() {
        caso.executar(CLIENTE, Paginacao.padrao());

        assertThat(notificacoes.ultimaPaginacao().ordem()).containsExactlyElementsOf(ListarNotificacoes.ORDEM_PADRAO);
        assertThat(ListarNotificacoes.ORDEM_PADRAO).containsExactly(
                new Ordem("lida", Ordem.Direcao.ASC),
                new Ordem("criadaEm", Ordem.Direcao.DESC),
                new Ordem("id", Ordem.Direcao.DESC));
    }

    @Test
    void dec13_respeitaAOrdenacaoInformadaPeloCliente() {
        Paginacao informada = new Paginacao(0, 5, List.of(new Ordem("criadaEm", Ordem.Direcao.ASC)));

        caso.executar(CLIENTE, informada);

        assertThat(notificacoes.ultimaPaginacao()).isEqualTo(informada);
    }

    @Test
    void dec13_listaSomenteAsNotificacoesDoUsuario() {
        criar(CLIENTE);
        criar(CLIENTE);
        criar(OUTRO_CLIENTE);

        assertThat(caso.executar(CLIENTE, Paginacao.padrao()).itens()).hasSize(2);
        assertThat(caso.executar(OUTRO_CLIENTE, Paginacao.padrao()).itens()).hasSize(1);
    }

    @Test
    void dec13_preservaOPaginaETamanhoDoEnvelope() {
        Paginacao paginacao = new Paginacao(2, 10, List.of());

        var pagina = caso.executar(CLIENTE, paginacao);

        assertThat(pagina.pagina()).isEqualTo(2);
        assertThat(pagina.tamanho()).isEqualTo(10);
    }

    private void criar(Long usuarioId) {
        notificacoes.salvar(Notificacao.nova(usuarioId, TipoNotificacao.PEDIDO_APROVADO,
                "Pedido aprovado.", "pedido", 10042L, CRIADA_EM));
    }
}
