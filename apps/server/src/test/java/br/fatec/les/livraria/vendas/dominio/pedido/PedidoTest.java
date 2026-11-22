package br.fatec.les.livraria.vendas.dominio.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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

    @Test
    void rnVen009_despachoLevaOPedidoAprovadoAoTransporte() {
        Pedido pedido = Pedidos.emProcessamento(10L, 1L);
        pedido.aprovar();

        pedido.despachar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_TRANSPORTE);
        assertThat(pedido.getEntregueEm()).isNull();
    }

    @Test
    void rnVen009_soOPedidoAprovadoPodeSerDespachado() {
        Pedido emProcessamento = Pedidos.emProcessamento(10L, 1L);
        Pedido reprovado = Pedidos.emProcessamento(10L, 1L);
        reprovado.reprovar();
        Pedido despachado = Pedidos.emProcessamento(10L, 1L);
        despachado.aprovar();
        despachado.despachar();

        assertThatThrownBy(emProcessamento::despachar).isInstanceOf(ExcecaoDeConflito.class);
        assertThatThrownBy(reprovado::despachar).isInstanceOf(ExcecaoDeConflito.class);
        assertThatThrownBy(despachado::despachar).isInstanceOf(ExcecaoDeConflito.class);
    }

    @Test
    void rnVen010_entregaConfirmadaGravaADataDaEntrega() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 24, 15, 30);
        Pedido pedido = Pedidos.emProcessamento(10L, 1L);
        pedido.aprovar();
        pedido.despachar();

        pedido.confirmarEntrega(agora);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(pedido.getEntregueEm()).isEqualTo(agora);
    }

    @Test
    void rnVen010_soOPedidoEmTransportePodeSerEntregueEEntregueEhFinal() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 24, 15, 30);
        Pedido aprovado = Pedidos.emProcessamento(10L, 1L);
        aprovado.aprovar();
        Pedido entregue = Pedidos.emProcessamento(10L, 1L);
        entregue.aprovar();
        entregue.despachar();
        entregue.confirmarEntrega(agora);

        assertThatThrownBy(() -> aprovado.confirmarEntrega(agora)).isInstanceOf(ExcecaoDeConflito.class);
        assertThatThrownBy(() -> entregue.confirmarEntrega(agora.plusDays(1))).isInstanceOf(ExcecaoDeConflito.class);
        assertThatThrownBy(entregue::despachar).isInstanceOf(ExcecaoDeConflito.class);
        assertThatThrownBy(entregue::reprovar).isInstanceOf(ExcecaoDeConflito.class);
        assertThat(entregue.getEntregueEm()).isEqualTo(agora);
    }
}
