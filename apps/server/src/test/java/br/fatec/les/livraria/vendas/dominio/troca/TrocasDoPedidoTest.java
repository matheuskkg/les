package br.fatec.les.livraria.vendas.dominio.troca;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;

class TrocasDoPedidoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private final Pedido pedido = Pedidos.entregue(AGORA.minusDays(1));
    private final ItemPedido domCasmurro = pedido.getItens().get(0);
    private final ItemPedido memorias = pedido.getItens().get(1);

    @Test
    void dec12_semTrocaTudoEstaDisponivelESemIndicador() {
        TrocasDoPedido trocas = new TrocasDoPedido(pedido, List.of());

        assertThat(trocas.quantidadeDisponivel(domCasmurro)).isEqualTo(2);
        assertThat(trocas.statusDoItem(domCasmurro)).isEmpty();
        assertThat(trocas.indicador()).isEmpty();
    }

    @Test
    void dec12_quantidadeDisponivelDescontaTodasAsTrocasInclusiveRecusadas() {
        TrocasDoPedido trocas = new TrocasDoPedido(pedido, List.of(
                troca(1L, StatusTroca.TROCA_RECUSADA, new ItemTroca(1L, 101L, 1, null)),
                troca(2L, StatusTroca.EM_TROCA, new ItemTroca(2L, 101L, 1, null))));

        assertThat(trocas.quantidadeDisponivel(domCasmurro)).isZero();
        assertThat(trocas.quantidadeDisponivel(memorias)).isEqualTo(1);
    }

    @Test
    void dec12_indicadorParcialComAMaisRecentePeloId() {
        TrocasDoPedido trocas = new TrocasDoPedido(pedido, List.of(
                troca(7L, StatusTroca.EM_TROCA, new ItemTroca(2L, 102L, 1, null)),
                troca(3L, StatusTroca.TROCADO, new ItemTroca(1L, 101L, 1, null))));

        assertThat(trocas.trocas()).extracting(Troca::getId).containsExactly(7L, 3L);
        assertThat(trocas.indicador())
                .contains(new IndicadorDeTroca(StatusTroca.EM_TROCA, AbrangenciaDaTroca.PARCIAL));
        assertThat(trocas.statusDoItem(domCasmurro)).contains(StatusTroca.TROCADO);
        assertThat(trocas.statusDoItem(memorias)).contains(StatusTroca.EM_TROCA);
    }

    @Test
    void dec12_indicadorTotalQuandoAsTrocasNaoRecusadasCobremOPedido() {
        TrocasDoPedido trocas = new TrocasDoPedido(pedido, List.of(
                troca(1L, StatusTroca.TROCA_AUTORIZADA, new ItemTroca(1L, 101L, 2, null)),
                troca(2L, StatusTroca.EM_TROCA, new ItemTroca(2L, 102L, 1, null))));

        assertThat(trocas.indicador())
                .contains(new IndicadorDeTroca(StatusTroca.EM_TROCA, AbrangenciaDaTroca.TOTAL));
    }

    @Test
    void dec12_trocaRecusadaNaoContaParaAAbrangenciaMasDefineASituacao() {
        TrocasDoPedido trocas = new TrocasDoPedido(pedido, List.of(
                troca(1L, StatusTroca.TROCA_RECUSADA, new ItemTroca(1L, 101L, 2, null),
                        new ItemTroca(2L, 102L, 1, null))));

        assertThat(trocas.indicador())
                .contains(new IndicadorDeTroca(StatusTroca.TROCA_RECUSADA, AbrangenciaDaTroca.PARCIAL));
        assertThat(trocas.statusDoItem(memorias)).contains(StatusTroca.TROCA_RECUSADA);
    }

    private static Troca troca(Long id, StatusTroca status, ItemTroca... itens) {
        return new Troca(id, 1L, status, AGORA, List.of(itens), null, null, null);
    }
}
