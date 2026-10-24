package br.fatec.les.livraria.vendas.dominio.troca;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.junit.jupiter.api.Test;

class PrazoDeTrocaTest {

    private static final LocalDateTime ENTREGUE_EM = LocalDateTime.of(2026, 10, 24, 15, 0);
    private static final Period SETE_DIAS = Period.ofDays(7);

    @Test
    void rnVen013_ultimoDiaEhAEntregaMaisOPrazo() {
        assertThat(PrazoDeTroca.ultimoDia(ENTREGUE_EM, SETE_DIAS)).isEqualTo(LocalDate.of(2026, 10, 31));
    }

    @Test
    void rnVen013_oUltimoDiaEntraInteiro() {
        assertThat(PrazoDeTroca.dentro(ENTREGUE_EM, SETE_DIAS, LocalDateTime.of(2026, 10, 31, 23, 59, 59))).isTrue();
        assertThat(PrazoDeTroca.dentro(ENTREGUE_EM, SETE_DIAS, LocalDateTime.of(2026, 11, 1, 0, 0))).isFalse();
    }
}
