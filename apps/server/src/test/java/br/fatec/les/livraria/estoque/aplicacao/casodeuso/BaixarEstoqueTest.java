package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** RF-EST-003, RN-EST-006, DEC-15.3: baixa da venda aprovada consumindo as reservas do checkout. */
class BaixarEstoqueTest {

    private static final LocalDateTime PRAZO = LocalDateTime.of(2026, 10, 24, 13, 0);

    private final Map<Long, Integer> estoque = new HashMap<>(Map.of(1L, 20, 2L, 5));
    private final ReservasEmMemoria reservas = new ReservasEmMemoria();
    private final BaixarEstoque baixar = new BaixarEstoque(new EstoqueEmMemoria(estoque), reservas);

    @Test
    void rfEst003_subtraiAsQuantidadesEApagaAsReservasDoCheckout() {
        reservas.salvar(Reserva.nova(1L, OrigemReserva.CHECKOUT, 10L, 2, PRAZO));
        reservas.salvar(Reserva.nova(2L, OrigemReserva.CHECKOUT, 10L, 5, PRAZO));
        reservas.salvar(Reserva.nova(1L, OrigemReserva.CARRINHO, 3L, 1, PRAZO));

        baixar.executar(OrigemReserva.CHECKOUT, 10L, Map.of(1L, 2, 2L, 5));

        assertThat(estoque).containsEntry(1L, 18).containsEntry(2L, 0);
        assertThat(reservas.todas).singleElement().extracting(Reserva::getOrigem).isEqualTo(OrigemReserva.CARRINHO);
    }

    @Test
    void rfEst003_baixaNaoDeixaOSaldoNegativo() {
        assertThatThrownBy(() -> baixar.executar(OrigemReserva.CHECKOUT, 10L, Map.of(2L, 6)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> baixar.executar(OrigemReserva.CHECKOUT, 10L, Map.of(99L, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(estoque).containsEntry(2L, 5);
    }
}
