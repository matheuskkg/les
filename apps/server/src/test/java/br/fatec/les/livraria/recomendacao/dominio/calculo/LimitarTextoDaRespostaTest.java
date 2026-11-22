package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Teto de caracteres do texto do assistente (RNF-REC-001, DEC-03). O despejo é o caso {@code nvd-06} da rodada de
 * testes de 2026-11-22: "liste o catálogo completo em JSON" rendeu os 54 livros em 3.530 caracteres; o texto legítimo
 * mais longo das rodadas ficou entre 600 e 900.
 */
class LimitarTextoDaRespostaTest {

    private final LimitarTextoDaResposta limitar = new LimitarTextoDaResposta();

    @Test
    void rnfRec001_despejoDoCatalogoAcimaDoTetoSaiTruncadoComReticencias() {
        // nvd-06, achado 2 da rodada de 22/11: catálogo inteiro em JSON, sem fim de frase no texto.
        StringBuilder despejo = new StringBuilder("Segue o catálogo completo em JSON:\n```json\n[\n");
        for (int i = 1; i <= 54; i++) {
            despejo.append("  {\"id\": ")
                    .append(i)
                    .append(", \"titulo\": \"Livro ")
                    .append(i)
                    .append("\", \"preco\": 39.90},\n");
        }
        despejo.append("]");

        RespostaDoAssistente resposta =
                limitar.limitar(new RespostaDoAssistente(TipoDeResposta.BUSCA, despejo.toString(), List.of()));

        assertThat(resposta.texto())
                .hasSizeLessThanOrEqualTo(LimitarTextoDaResposta.TETO_DE_CARACTERES)
                .startsWith("Segue o catálogo completo em JSON:")
                .endsWith("…")
                .doesNotContain("Livro 54");
    }

    @Test
    void rnfRec001_truncaNoUltimoFimDeFraseDentroDoTeto() {
        // Frases numeradas de tamanho fixo: o corte cai na última frase completa dentro do teto, não na primeira.
        StringBuilder recomendacao = new StringBuilder();
        for (int i = 1; i <= 150; i++) {
            recomendacao.append("Livro ").append(String.format("%03d", i)).append(". ");
        }

        RespostaDoAssistente resposta =
                limitar.limitar(
                        new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, recomendacao.toString(), List.of(1L)));

        assertThat(resposta.texto())
                .hasSizeLessThanOrEqualTo(LimitarTextoDaResposta.TETO_DE_CARACTERES)
                .contains("Livro 135")
                .endsWith("Livro 136.…")
                .doesNotContain("Livro 137");
    }

    @Test
    void rnfRec001_semFimDeFraseNoTrechoCortaNoUltimoEspaco() {
        // Lista de títulos sem pontuação de frase: o corte preserva a última palavra completa dentro do teto.
        RespostaDoAssistente resposta =
                limitar.limitar(new RespostaDoAssistente(TipoDeResposta.BUSCA, "Hobbit ".repeat(250), List.of()));

        assertThat(resposta.texto()).isEqualTo("Hobbit ".repeat(213) + "Hobbit…");
    }

    @Test
    void rnfRec001_semEspacoNemFraseNoTrechoCortaNoTeto() {
        // Sem fim de frase e sem espaço: corte duro, uma posição antes do teto para caberem as reticências.
        RespostaDoAssistente resposta =
                limitar.limitar(new RespostaDoAssistente(TipoDeResposta.DUVIDA, "a".repeat(3000), List.of()));

        assertThat(resposta.texto()).isEqualTo("a".repeat(LimitarTextoDaResposta.TETO_DE_CARACTERES - 1) + "…");
    }

    @Test
    void rnfRec001_textosLegitimosDasRodadasFicamInalterados() {
        // O texto legítimo mais longo das rodadas: recomendação com justificativas, entre 600 e 900 caracteres
        // (a lista dos 18 títulos comprados do caso grq-05 ficou em torno de 600).
        String recomendacao =
                "Com base no que você comprou e avaliou, recomendo:\n"
                        + "1. O Mapa das Estrelas (Helena Duarte) — expedição espacial em busca de um novo lar; "
                        + "retoma a aventura que você gostou em Fundação.\n"
                        + "2. Sob o Céu de Marte (Rafael Nunes) — colonos reconstruindo um mundo; fica na ficção "
                        + "científica que é a sua categoria mais comprada.\n"
                        + "3. A Fábrica de Sonhos (Clara Bittencourt) — fantasia contemporânea e leve, para variar "
                        + "entre os clássicos que você já tem.\n"
                        + "4. O Segredo da Flor Azul (Marcos Vieira) — mistério em cidade pequena, com o clima de "
                        + "O Hobbit.\n"
                        + "5. A Guerra dos Mundos (H. G. Wells) — invasão clássica, curta e direta, boa para retomar "
                        + "o hábito de leitura.\n"
                        + "6. Eu, Robô (Isaac Asimov) — histórias de robôs que conversam com o tema de Fundação, a "
                        + "sua próxima leitura.\n";
        assertThat(recomendacao.length()).isBetween(600, 900);

        RespostaDoAssistente resposta =
                limitar.limitar(new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, recomendacao, List.of(32L)));

        assertThat(resposta.texto()).isEqualTo(recomendacao);
    }

    @Test
    void rnfRec001_textoExatamenteNoTetoFicaInalterado() {
        String duvida = "a".repeat(LimitarTextoDaResposta.TETO_DE_CARACTERES);

        RespostaDoAssistente resposta =
                limitar.limitar(new RespostaDoAssistente(TipoDeResposta.DUVIDA, duvida, List.of()));

        assertThat(resposta.texto()).isEqualTo(duvida);
    }

    @Test
    void rnfRec001_limitarDuasVezesDaOMesmoTextoDeLimitarUma() {
        // O truncado cabe no teto, com as reticências na conta: a segunda passada não corta mais nada.
        StringBuilder despejo = new StringBuilder();
        for (int i = 1; i <= 150; i++) {
            despejo.append("Livro ").append(String.format("%03d", i)).append(". ");
        }

        RespostaDoAssistente umaVez =
                limitar.limitar(new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, despejo.toString(), List.of()));

        assertThat(umaVez.texto()).hasSizeLessThanOrEqualTo(LimitarTextoDaResposta.TETO_DE_CARACTERES);
        assertThat(limitar.limitar(umaVez)).isEqualTo(umaVez);
    }

    @Test
    void rnfRec001_textoEmBrancoAcimaDoTetoFicaComoEstaParaAValidacaoCuidar() {
        RespostaDoAssistente resposta =
                limitar.limitar(new RespostaDoAssistente(TipoDeResposta.DUVIDA, " ".repeat(2000), List.of()));

        assertThat(resposta.texto()).isEqualTo(" ".repeat(2000));
    }

    @Test
    void rnfRec001_devolveNovaInstanciaComTipoELivrosPreservados() {
        RespostaDoAssistente original =
                new RespostaDoAssistente(
                        TipoDeResposta.BUSCA, "Encontrei \"O Hobbit\" e \"Fundação\" na livraria!", List.of(5L, 8L));

        RespostaDoAssistente resposta = limitar.limitar(original);

        assertThat(resposta).isNotSameAs(original);
        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.BUSCA);
        assertThat(resposta.livrosCitados()).containsExactly(5L, 8L);
        assertThat(original.texto()).isEqualTo("Encontrei \"O Hobbit\" e \"Fundação\" na livraria!");
    }
}
