package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class RemoverLivrosCompradosDaRecomendacaoTest {

    private static final Set<Long> COMPRADOS = Set.of(1L, 3L);

    private final RemoverLivrosCompradosDaRecomendacao remover = new RemoverLivrosCompradosDaRecomendacao();

    @Test
    void rnfRec001_recomendacaoRemoveOsLivrosComprados() {
        RespostaDoAssistente resposta =
                remover.remover(
                        new RespostaDoAssistente(
                                TipoDeResposta.RECOMENDACAO, "Separei estes para você.", List.of(1L, 2L, 3L, 4L)),
                        COMPRADOS);

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.RECOMENDACAO);
        assertThat(resposta.texto()).isEqualTo("Separei estes para você.");
        assertThat(resposta.livrosCitados()).containsExactly(2L, 4L);
    }

    @Test
    void rnfRec001_buscaMantemOsLivrosComprados() {
        RespostaDoAssistente resposta =
                remover.remover(
                        new RespostaDoAssistente(TipoDeResposta.BUSCA, "Encontrei estes livros.", List.of(1L, 2L)),
                        COMPRADOS);

        assertThat(resposta.livrosCitados()).containsExactly(1L, 2L);
    }

    @Test
    void rnfRec001_duvidaMantemOsLivrosComprados() {
        RespostaDoAssistente resposta =
                remover.remover(
                        new RespostaDoAssistente(
                                TipoDeResposta.DUVIDA, "O livro que você tem custa R$ 29,90.", List.of(3L)),
                        COMPRADOS);

        assertThat(resposta.livrosCitados()).containsExactly(3L);
    }

    @Test
    void rnfRec001_foraDoEscopoSemLivros() {
        RespostaDoAssistente resposta =
                remover.remover(
                        new RespostaDoAssistente(
                                TipoDeResposta.FORA_DO_ESCOPO,
                                ValidarRespostaDoAssistente.TEXTO_FORA_DO_ESCOPO,
                                List.of()),
                        COMPRADOS);

        assertThat(resposta.livrosCitados()).isEmpty();
        assertThat(resposta.texto()).isEqualTo(ValidarRespostaDoAssistente.TEXTO_FORA_DO_ESCOPO);
    }

    @Test
    void rnfRec001_semLivrosRestantesFicaSemCitadosParaAIndicacaoDosQueFaltam() {
        // Caso gem-07a da rodada de 22/11 (achado 7): o modelo citou só clássicos que a cliente já tinha e o server
        // os removeu. A remoção só esvazia os citados; quem troca o texto e indica os livros que ela ainda não tem é
        // IndicarLivrosQueOClienteNaoTem, chamada pelo caso de uso nesse caso (ALT-01-rec08-so-novos).
        RespostaDoAssistente resposta =
                remover.remover(
                        new RespostaDoAssistente(
                                TipoDeResposta.RECOMENDACAO, "Recomendo estes dois livros.", List.of(1L, 3L)),
                        COMPRADOS);

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.RECOMENDACAO);
        assertThat(resposta.livrosCitados()).isEmpty();
        assertThat(resposta.texto()).isEqualTo("Recomendo estes dois livros.");
    }

    @Test
    void rnfRec001_buscaComTodosCompradosMantemLivrosETexto() {
        // A indicação dos livros que faltam é só da recomendação: na busca, o cliente pode perguntar de um livro
        // que já tem, então nem os livros citados nem o texto mudam (caso gem-07a da rodada de 22/11, achado 7).
        RespostaDoAssistente resposta =
                remover.remover(
                        new RespostaDoAssistente(TipoDeResposta.BUSCA, "Temos estes títulos.", List.of(1L, 3L)),
                        COMPRADOS);

        assertThat(resposta.livrosCitados()).containsExactly(1L, 3L);
        assertThat(resposta.texto()).isEqualTo("Temos estes títulos.");
    }
}
