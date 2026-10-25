package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

class ValidarRespostaDoAssistenteTest {

    private final ValidarRespostaDoAssistente validar = new ValidarRespostaDoAssistente();

    @Test
    void rnfRec001_foraDoEscopoTrocaOTextoDoModeloPeloFixo() {
        RespostaDoAssistente resposta = validar.validar(
                new RespostaDoAssistente(TipoDeResposta.FORA_DO_ESCOPO, "O jogo foi 2 a 1.", List.of(1L)),
                Set.of(1L));

        assertThat(resposta.texto()).isEqualTo(ValidarRespostaDoAssistente.TEXTO_FORA_DO_ESCOPO);
        assertThat(resposta.livrosCitados()).isEmpty();
    }

    @Test
    void rnfRec001_removeLivrosInexistentesInativosOuSemEstoqueERepetidos() {
        RespostaDoAssistente resposta = validar.validar(
                new RespostaDoAssistente(TipoDeResposta.BUSCA, " Veja: ", List.of(3L, 99L, 1L, 3L)),
                Set.of(1L, 3L));

        assertThat(resposta.livrosCitados()).containsExactly(3L, 1L);
        assertThat(resposta.texto()).isEqualTo("Veja:");
        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.BUSCA);
    }

    @Test
    void rnfRec001_limitaOsLivrosCitados() {
        List<Long> muitos = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);

        assertThat(validar.validar(new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, "Estes.", muitos),
                Set.copyOf(muitos)).livrosCitados()).hasSize(ValidarRespostaDoAssistente.LIMITE_DE_LIVROS);
    }

    @Test
    void rnfRec001_textoEmBrancoViraTextoPadrao() {
        assertThat(validar.validar(new RespostaDoAssistente(TipoDeResposta.BUSCA, " ", List.of(1L)), Set.of(1L))
                .texto()).isEqualTo(ValidarRespostaDoAssistente.TEXTO_SO_COM_LIVROS);
        assertThat(validar.validar(new RespostaDoAssistente(TipoDeResposta.DUVIDA, "", List.of()), Set.of())
                .texto()).isEqualTo(ValidarRespostaDoAssistente.TEXTO_SEM_RESPOSTA);
    }
}
