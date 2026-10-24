package br.fatec.les.livraria.vendas.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;
import br.fatec.les.livraria.vendas.dominio.troca.ItemTroca;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca.ItemSolicitado;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;
import br.fatec.les.livraria.vendas.dominio.troca.TrocasDoPedido;

class RegrasDaSolicitacaoDeTrocaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final Period PRAZO = Period.ofDays(7);

    @Test
    void rnVen013_pedidoNaoEntregueNaoPodeSerTrocado() {
        Pedido aprovado = Pedidos.emProcessamento(10L, 1L);
        aprovado.aprovar();

        assertThat(violacoes(new PedidoEntregueParaTroca(), solicitacao(aprovado, List.of(), item(101L, 1))))
                .extracting(Violacao::codigo).containsExactly("RN-VEN-013");
        assertThat(violacoes(new PedidoEntregueParaTroca(), solicitacao(entregueHaDias(1), List.of(), item(101L, 1))))
                .isEmpty();
    }

    @Test
    void rnVen013_trocaSoAteOFimDoUltimoDiaDoPrazo() {
        assertThat(violacoes(new DentroDoPrazoDeTroca(), solicitacao(entregueHaDias(7), List.of(), item(101L, 1))))
                .isEmpty();
        assertThat(violacoes(new DentroDoPrazoDeTroca(), solicitacao(entregueHaDias(8), List.of(), item(101L, 1))))
                .extracting(Violacao::codigo).containsExactly("RN-VEN-013");
    }

    @Test
    void rfVen011_exigeAoMenosUmItem() {
        Pedido pedido = entregueHaDias(1);

        assertThat(violacoes(new TrocaComItens(), solicitacao(pedido, List.of()))).hasSize(1);
        assertThat(violacoes(new TrocaComItens(), solicitacao(pedido, List.of(), item(101L, 1)))).isEmpty();
    }

    @Test
    void rfVen011_itensSaoDoProprioPedido() {
        Pedido pedido = entregueHaDias(1);
        ItensDaTrocaDoPedido regra = new ItensDaTrocaDoPedido();

        assertThat(violacoes(regra, solicitacao(pedido, List.of(), item(999L, 1)))).hasSize(1);
        assertThat(violacoes(regra, solicitacao(pedido, List.of(), new ItemSolicitado(null, 1)))).hasSize(1);
        assertThat(violacoes(regra, solicitacao(pedido, List.of(), item(101L, 1), item(102L, 1)))).isEmpty();
    }

    @Test
    void rfVen011_itemNaoSeRepeteNaTroca() {
        Pedido pedido = entregueHaDias(1);
        ItensDaTrocaSemRepeticao regra = new ItensDaTrocaSemRepeticao();

        assertThat(violacoes(regra, solicitacao(pedido, List.of(), item(101L, 1), item(101L, 1)))).hasSize(1);
        assertThat(violacoes(regra, solicitacao(pedido, List.of(), item(101L, 1), item(102L, 1)))).isEmpty();
    }

    @Test
    void rfVen011_quantidadeEntreUmEADisponivelDescontandoTrocasRecusadas() {
        Pedido pedido = entregueHaDias(1);
        List<Troca> anteriores = List.of(new Troca(1L, 1L, StatusTroca.TROCA_RECUSADA, AGORA,
                List.of(new ItemTroca(1L, 102L, 1, null)), "Item danificado pelo uso", null, null));
        QuantidadeDisponivelParaTroca regra = new QuantidadeDisponivelParaTroca();

        assertThat(violacoes(regra, solicitacao(pedido, anteriores, item(101L, 2)))).isEmpty();
        assertThat(violacoes(regra, solicitacao(pedido, anteriores, item(101L, 3)))).hasSize(1);
        assertThat(violacoes(regra, solicitacao(pedido, anteriores, item(101L, 0)))).hasSize(1);
        assertThat(violacoes(regra, solicitacao(pedido, anteriores, item(102L, 1))))
                .extracting(Violacao::mensagem).first().asString().contains("não tem mais unidades");
    }

    private static Pedido entregueHaDias(int dias) {
        return Pedidos.entregue(AGORA.minusDays(dias));
    }

    private static ItemSolicitado item(Long itemPedidoId, int quantidade) {
        return new ItemSolicitado(itemPedidoId, quantidade);
    }

    private static SolicitacaoDeTroca solicitacao(Pedido pedido, List<Troca> trocas, ItemSolicitado... itens) {
        return new SolicitacaoDeTroca(new TrocasDoPedido(pedido, trocas), Arrays.asList(itens), PRAZO, AGORA);
    }

    private static List<Violacao> violacoes(Regra<SolicitacaoDeTroca> regra, SolicitacaoDeTroca alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes);
        return violacoes.lista();
    }
}
