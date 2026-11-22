package br.fatec.les.livraria.vendas.dominio.troca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class ValorDaTrocaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private final Pedido pedido = Pedidos.entregue(AGORA.minusDays(1));

    @Test
    void rfVen015_somaPrecoUnitarioVezesQuantidadeSemFrete() {
        Troca troca = troca(new ItemTroca(1L, 101L, 2, null), new ItemTroca(2L, 102L, 1, null));

        assertThat(ValorDaTroca.de(troca, pedido)).isEqualTo(Dinheiro.de("135.66"));
        assertThat(ValorDaTroca.de(troca(new ItemTroca(1L, 101L, 1, null)), pedido)).isEqualTo(Dinheiro.de("47.88"));
    }

    @Test
    void rfVen015_itemDeOutroPedidoFalha() {
        assertThatThrownBy(() -> ValorDaTroca.de(troca(new ItemTroca(1L, 999L, 1, null)), pedido))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Troca troca(ItemTroca... itens) {
        return new Troca(1L, 1L, StatusTroca.TROCA_AUTORIZADA, AGORA, List.of(itens), null, null, null);
    }
}
