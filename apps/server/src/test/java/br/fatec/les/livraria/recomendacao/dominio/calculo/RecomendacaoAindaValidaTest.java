package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.recomendacao.dominio.modelo.Recomendacao;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class RecomendacaoAindaValidaTest {

    private static final LocalDateTime GERADA = LocalDateTime.of(2026, 10, 25, 10, 0);
    private final RecomendacaoAindaValida regra = new RecomendacaoAindaValida();

    private static Recomendacao comIa(int compras) {
        return new Recomendacao(1L, 1L, 10L, GERADA, compras, List.of());
    }

    @Test
    void rnfRec001_valeAte24HorasSemCompraNova() {
        assertThat(regra.valida(comIa(2), GERADA.plusHours(23), 2)).isTrue();
        assertThat(regra.valida(comIa(2), GERADA.plusHours(24), 2)).isFalse();
    }

    @Test
    void rnfRec001_compraQuePassaAContarComoVendaInvalida() {
        // Pedido realizado antes da geração e aprovado depois: o número de compras muda.
        assertThat(regra.valida(comIa(2), GERADA.plusMinutes(5), 3)).isFalse();
    }

    @Test
    void rnfRec001_gravadaSemIaValeUmaHora() {
        Recomendacao semIa = new Recomendacao(1L, 1L, null, GERADA, 0, List.of());

        assertThat(regra.valida(semIa, GERADA.plusMinutes(59), 0)).isTrue();
        assertThat(regra.valida(semIa, GERADA.plusHours(1), 0)).isFalse();
    }
}
