package br.fatec.les.livraria.vendas.aplicacao.consulta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;

class PeriodoDoPedidoTest {

    @Test
    void rfVen009_ultimoDiaEntraInteiro() {
        PeriodoDoPedido periodo = PeriodoDoPedido.dosDias(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 24));

        assertThat(periodo.inicio()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
        assertThat(periodo.fimExclusivo()).isEqualTo(LocalDateTime.of(2026, 10, 25, 0, 0));
    }

    @Test
    void rfVen009_mesmoDiaCobreODiaTodo() {
        PeriodoDoPedido periodo = PeriodoDoPedido.dosDias(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 24));

        assertThat(periodo.inicio()).isEqualTo(LocalDateTime.of(2026, 10, 24, 0, 0));
        assertThat(periodo.fimExclusivo()).isEqualTo(LocalDateTime.of(2026, 10, 25, 0, 0));
    }

    @Test
    void rfVen009_limiteAusenteNaoFiltra() {
        assertThat(PeriodoDoPedido.dosDias(null, null)).isEqualTo(new PeriodoDoPedido(null, null));
        assertThat(PeriodoDoPedido.dosDias(LocalDate.of(2026, 10, 1), null).fimExclusivo()).isNull();
        assertThat(PeriodoDoPedido.dosDias(null, LocalDate.of(2026, 10, 1)).inicio()).isNull();
    }

    @Test
    void dec07_periodoInvertidoEhParametroInvalido() {
        assertThatThrownBy(() -> PeriodoDoPedido.dosDias(LocalDate.of(2026, 10, 25), LocalDate.of(2026, 10, 24)))
                .isInstanceOf(ParametroInvalido.class);
    }

    @Test
    void dec07_ultimoDiaRepresentavelEhParametroInvalido() {
        assertThatThrownBy(() -> PeriodoDoPedido.dosDias(null, LocalDate.MAX)).isInstanceOf(ParametroInvalido.class);
    }
}
