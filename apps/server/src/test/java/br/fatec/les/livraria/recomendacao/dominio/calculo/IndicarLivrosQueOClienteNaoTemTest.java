package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Indicação dos livros que o cliente ainda não tem quando a recomendação do chat perde todos os citados (RNF-REC-001,
 * DEC-03; caso gem-07a, achado 7 da rodada de testes de 22/11, ALT-01-rec08-so-novos).
 */
class IndicarLivrosQueOClienteNaoTemTest {

    private final IndicarLivrosQueOClienteNaoTem indicar = new IndicarLivrosQueOClienteNaoTem();

    @Test
    void rnfRec001_recomendacaoSemCitadosIndicaOsLivrosQueOClienteNaoTem() {
        // Caso gem-07a: o modelo citou só clássicos que a cliente já tinha, o server os removeu — em vez do texto
        // fixo de ALT-10, a resposta indica os livros que ela ainda não tem, que viram cartões pelo campo de citados.
        RespostaDoAssistente resposta =
                indicar.indicar(
                        new RespostaDoAssistente(
                                TipoDeResposta.RECOMENDACAO, "Recomendo estes dois livros.", List.of()),
                        List.of(7L, 8L, 9L));

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.RECOMENDACAO);
        assertThat(resposta.texto()).isEqualTo(IndicarLivrosQueOClienteNaoTem.TEXTO_COM_LIVROS_NOVOS);
        assertThat(resposta.livrosCitados()).containsExactly(7L, 8L, 9L);
    }

    @Test
    void rnfRec001_indicaNoMaximoOLimiteDeLivrosCitadosPorResposta() {
        RespostaDoAssistente resposta =
                indicar.indicar(
                        new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, "Recomendo este.", List.of()),
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));

        assertThat(resposta.livrosCitados()).containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(resposta.texto()).isEqualTo(IndicarLivrosQueOClienteNaoTem.TEXTO_COM_LIVROS_NOVOS);
    }

    @Test
    void rnfRec001_semCandidatosFicaComOTextoFixoSemLivros() {
        // O cliente já tem ou avaliou tudo à venda: dizer que ele já tem o que seria indicado volta a ser fato.
        RespostaDoAssistente resposta =
                indicar.indicar(
                        new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, "Recomendo estes.", List.of()),
                        List.of());

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.RECOMENDACAO);
        assertThat(resposta.texto()).isEqualTo(IndicarLivrosQueOClienteNaoTem.TEXTO_SEM_LIVROS_NOVOS);
        assertThat(resposta.livrosCitados()).isEmpty();
    }

    @Test
    void rnfRec001_recomendacaoQueAindaCitaLivrosNaoMuda() {
        RespostaDoAssistente original =
                new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, "Separei estes para você.", List.of(2L, 4L));

        RespostaDoAssistente resposta = indicar.indicar(original, List.of(7L));

        assertThat(resposta).isSameAs(original);
    }

    @Test
    void rnfRec001_buscaSemLivrosNaoViraIndicacao() {
        // O fallback é da recomendação: a busca pode voltar sem livros sem indicar os candidatos.
        RespostaDoAssistente original =
                new RespostaDoAssistente(TipoDeResposta.BUSCA, "Não encontrei este título.", List.of());

        RespostaDoAssistente resposta = indicar.indicar(original, List.of(7L));

        assertThat(resposta).isSameAs(original);
    }
}
