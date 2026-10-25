package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class Percentil95Test {

    private final Percentil95 percentil = new Percentil95();

    @Test
    void rnfRec001_usaOPostoMaisProximoSemInterpolar() {
        List<Integer> cem = IntStream.rangeClosed(1, 100).boxed().toList().reversed();
        assertThat(percentil.calcular(cem)).isEqualTo(95);
        assertThat(percentil.calcular(List.of(300, 100, 200))).isEqualTo(300);
        assertThat(percentil.calcular(List.of(42))).isEqualTo(42);
    }

    @Test
    void rnfRec001_semAmostrasNaoHaPercentil() {
        assertThat(percentil.calcular(List.of())).isNull();
    }
}
