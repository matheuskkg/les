package br.fatec.les.livraria.vendas.aplicacao.consulta;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class ItemVendidoTest {

    @Test
    void rnAna001_valorEhOPrecoVezesAQuantidadeNaoTrocada_DEC_12() {
        ItemVendido item =
                new ItemVendido(LocalDateTime.of(2025, 3, 12, 14, 30), List.of(), Dinheiro.de("48.00"), 3, 1);

        assertThat(item.valor()).isEqualTo(Dinheiro.de("96.00"));
    }

    @Test
    void rnAna004_contamComoVendaAprovadaEmTransporteEEntregue() {
        assertThat(StatusPedido.values())
                .filteredOn(StatusPedido::contaComoVenda)
                .containsExactly(StatusPedido.APROVADA, StatusPedido.EM_TRANSPORTE, StatusPedido.ENTREGUE);
    }
}
