package br.fatec.les.livraria.vendas.dominio.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;

class PedidoTest {

    @Test
    void rfVen008_finalizacaoCriaOPedidoEmProcessamentoComOsValores() {
        Pedido pedido = Pedidos.emProcessamento(10L, 1L);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_PROCESSAMENTO);
        assertThat(pedido.getSubtotal().valor()).isEqualByComparingTo("95.76");
        assertThat(pedido.getTotal().valor()).isEqualByComparingTo("97.76");
    }

    @Test
    void rnVen008_operadoraAprovaOuReprovaOPedidoEmProcessamento() {
        Pedido aprovado = Pedidos.emProcessamento(10L, 1L);
        aprovado.aprovar();
        Pedido reprovado = Pedidos.emProcessamento(10L, 1L);
        reprovado.reprovar();

        assertThat(aprovado.getStatus()).isEqualTo(StatusPedido.APROVADA);
        assertThat(reprovado.getStatus()).isEqualTo(StatusPedido.REPROVADA);
    }

    @Test
    void rnVen008_pedidoForaDeProcessamentoNaoMudaDeResultado() {
        Pedido pedido = Pedidos.emProcessamento(10L, 1L);
        pedido.reprovar();

        assertThatThrownBy(pedido::aprovar).isInstanceOf(ExcecaoDeConflito.class);
        assertThatThrownBy(pedido::reprovar).isInstanceOf(ExcecaoDeConflito.class);
    }

    @Test
    void rnVen007_soOsCuponsDeTrocaSaoDevolvidosPeloIdDaCopia() {
        assertThat(Pedidos.emProcessamento(10L, 1L).cuponsDeTrocaUsados()).containsExactly(2L);
    }
}
