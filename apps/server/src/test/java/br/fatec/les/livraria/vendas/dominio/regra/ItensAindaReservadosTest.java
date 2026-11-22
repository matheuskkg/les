package br.fatec.les.livraria.vendas.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;
import br.fatec.les.livraria.vendas.dominio.pagamento.ReservasDoCheckout;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ItensAindaReservadosTest {

    private static final List<ItemCheckout> ITENS = List.of(new ItemCheckout(1L, 2), new ItemCheckout(2L, 1));

    @Test
    void rnVen002_itensReservadosComAMesmaQuantidadeSeguemParaOPedido() {
        assertThat(violacoes(Map.of(1L, 2, 2L, 1))).isEmpty();
    }

    @Test
    void rnVen002_itemSemReservaOuComOutraQuantidadeBarraAFinalizacao() {
        assertThat(violacoes(Map.of(1L, 2))).extracting(Violacao::codigo).containsExactly("RN-VEN-002");
        assertThat(violacoes(Map.of(1L, 1, 2L, 1))).extracting(Violacao::codigo).containsExactly("RN-VEN-002");
    }

    private static List<Violacao> violacoes(Map<Long, Integer> reservadas) {
        Violacoes violacoes = new Violacoes();
        new ItensAindaReservados().validar(new ReservasDoCheckout(ITENS, reservadas), violacoes);
        return violacoes.lista();
    }
}
