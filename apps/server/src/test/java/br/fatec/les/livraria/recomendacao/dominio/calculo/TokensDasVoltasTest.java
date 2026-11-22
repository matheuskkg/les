package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.recomendacao.dominio.modelo.VoltaDaChamada;

import org.junit.jupiter.api.Test;

import java.util.List;

/** Soma e descrição dos tokens das voltas de uma chamada (RNF-REC-001, DEC-03). */
class TokensDasVoltasTest {

    @Test
    void rnfRec001_somaAsVoltasEIgnoraAsSemTokens() {
        TokensDasVoltas.Resumo resumo =
                TokensDasVoltas.calcular(
                        List.of(new VoltaDaChamada(80, 10), VoltaDaChamada.semTokens(), new VoltaDaChamada(120, 30)));

        assertThat(resumo.entrada()).isEqualTo(200);
        assertThat(resumo.saida()).isEqualTo(40);
        assertThat(resumo.descricao()).isEqualTo("80/10;-/-;120/30");
    }

    @Test
    void rnfRec001_voltaSemNenhumTokenInformadoFicaComSomasNulas() {
        TokensDasVoltas.Resumo resumo = TokensDasVoltas.calcular(List.of(VoltaDaChamada.semTokens()));

        assertThat(resumo.entrada()).isNull();
        assertThat(resumo.saida()).isNull();
        assertThat(resumo.descricao()).isEqualTo("-/-");
    }

    @Test
    void rnfRec001_semVoltasFicaTudoNulo() {
        TokensDasVoltas.Resumo resumo = TokensDasVoltas.calcular(List.of());

        assertThat(resumo.entrada()).isNull();
        assertThat(resumo.saida()).isNull();
        assertThat(resumo.descricao()).isNull();
    }
}
