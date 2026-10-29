package br.fatec.les.livraria.recomendacao.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Truncamento de {@code chamada_ia.tokens_das_voltas} sem partir uma volta (RNF-REC-001, DEC-03).
 */
class ChamadaIaTest {

    @Test
    void rnfRec001_truncaTokensDasVoltasNaUltimaFronteiraDeVolta() {
        String tokens = "1/1;".repeat(50) + "2/2";

        ChamadaIa chamada = chamada(tokens);

        assertThat(chamada.tokensDasVoltas()).isEqualTo("1/1;".repeat(49) + "1/1");
    }

    @Test
    void rnfRec001_semFronteiraDeVoltaNoLimiteFicaNulo() {
        ChamadaIa chamada = chamada("1".repeat(ChamadaIa.LIMITE_DOS_TOKENS_DAS_VOLTAS + 1));

        assertThat(chamada.tokensDasVoltas()).isNull();
    }

    private static ChamadaIa chamada(String tokensDasVoltas) {
        return new ChamadaIa(1L, "modelo", OperacaoIa.CONVERSAR, LocalDateTime.now(), 10, 1,
                ResultadoDaChamada.SUCESSO, null, null, null, null, 1, tokensDasVoltas);
    }
}
