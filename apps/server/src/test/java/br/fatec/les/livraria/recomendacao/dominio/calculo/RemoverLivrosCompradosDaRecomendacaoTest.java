package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

class RemoverLivrosCompradosDaRecomendacaoTest {

    private static final Set<Long> COMPRADOS = Set.of(1L, 3L);

    private final RemoverLivrosCompradosDaRecomendacao remover = new RemoverLivrosCompradosDaRecomendacao();

    @Test
    void rnfRec001_recomendacaoRemoveOsLivrosComprados() {
        RespostaDoAssistente resposta = remover.remover(
                new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, "Separei estes para você.",
                        List.of(1L, 2L, 3L, 4L)),
                COMPRADOS);

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.RECOMENDACAO);
        assertThat(resposta.texto()).isEqualTo("Separei estes para você.");
        assertThat(resposta.livrosCitados()).containsExactly(2L, 4L);
    }

    @Test
    void rnfRec001_buscaMantemOsLivrosComprados() {
        RespostaDoAssistente resposta = remover.remover(
                new RespostaDoAssistente(TipoDeResposta.BUSCA, "Encontrei estes livros.", List.of(1L, 2L)),
                COMPRADOS);

        assertThat(resposta.livrosCitados()).containsExactly(1L, 2L);
    }

    @Test
    void rnfRec001_duvidaMantemOsLivrosComprados() {
        RespostaDoAssistente resposta = remover.remover(
                new RespostaDoAssistente(TipoDeResposta.DUVIDA, "O livro que você tem custa R$ 29,90.",
                        List.of(3L)),
                COMPRADOS);

        assertThat(resposta.livrosCitados()).containsExactly(3L);
    }

    @Test
    void rnfRec001_foraDoEscopoSemLivros() {
        RespostaDoAssistente resposta = remover.remover(
                new RespostaDoAssistente(TipoDeResposta.FORA_DO_ESCOPO,
                        ValidarRespostaDoAssistente.TEXTO_FORA_DO_ESCOPO, List.of()),
                COMPRADOS);

        assertThat(resposta.livrosCitados()).isEmpty();
        assertThat(resposta.texto()).isEqualTo(ValidarRespostaDoAssistente.TEXTO_FORA_DO_ESCOPO);
    }

    @Test
    void rnfRec001_semLivrosRestantesUsaOTextoPadrao() {
        RespostaDoAssistente resposta = remover.remover(
                new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO,
                        ValidarRespostaDoAssistente.TEXTO_SO_COM_LIVROS, List.of(1L, 3L)),
                COMPRADOS);

        assertThat(resposta.livrosCitados()).isEmpty();
        assertThat(resposta.texto()).isEqualTo(ValidarRespostaDoAssistente.TEXTO_SEM_RESPOSTA);
    }
}
