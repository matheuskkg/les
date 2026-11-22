package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Higiene dos ids internos do texto do assistente (RNF-REC-001, DEC-03), com os casos observados nas rodadas de testes
 * com os provedores reais (2026-11-22 e 2026-10-28).
 */
class HigienizarIdsDoTextoTest {

    private final HigienizarIdsDoTexto higienizar = new HigienizarIdsDoTexto();

    @Test
    void rnfRec001_removeAnotacaoDeIdEntreParenteses() {
        // gem-01: "recomendo "O Mapa das Estrelas" (id 32) e "O Segredo da Flor Azul" (id 52)!"
        RespostaDoAssistente resposta =
                higienizar.higienizar(
                        new RespostaDoAssistente(
                                TipoDeResposta.RECOMENDACAO,
                                "recomendo \"O Mapa das Estrelas\" (id 32) e \"O Segredo da Flor Azul\" (id 52)!",
                                List.of(32L, 52L)));

        assertThat(resposta.texto()).isEqualTo("recomendo \"O Mapa das Estrelas\" e \"O Segredo da Flor Azul\"!");
        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.RECOMENDACAO);
        assertThat(resposta.livrosCitados()).containsExactly(32L, 52L);
    }

    @Test
    void rnfRec001_removeAnotacoesComSeparadorColchetesCaixaEEspacamentoVariaveis() {
        // nvd-01, nemotron e nvd-05: "(id: 8)", "[ID 5]", "( id 24 )" e a pontuação que sobra.
        RespostaDoAssistente resposta =
                higienizar.higienizar(
                        new RespostaDoAssistente(
                                TipoDeResposta.RECOMENDACAO,
                                "Temos: Fundação (id: 8), O Hobbit [ID 5] e A Guerra dos Mundos ( id 24 ).",
                                List.of(8L, 5L, 24L)));

        assertThat(resposta.texto()).isEqualTo("Temos: Fundação, O Hobbit e A Guerra dos Mundos.");
    }

    @Test
    void rnfRec001_removeIdNoFimDaListaSemDeixarEspacoAntesDaQuebraDeLinha() {
        // nemotron (rodada de 28/10): "(id 8)" no fim da linha de cada item.
        RespostaDoAssistente resposta =
                higienizar.higienizar(
                        new RespostaDoAssistente(
                                TipoDeResposta.BUSCA,
                                "Temos estes livros de ficção científica:\n"
                                        + "- Fundação – Isaac Asimov – R$ 49,44 (id 8)\n"
                                        + "- Eu, Robô – Isaac Asimov – R$ 52,80 (id 9)",
                                List.of(8L, 9L)));

        assertThat(resposta.texto())
                .isEqualTo(
                        "Temos estes livros de ficção científica:\n"
                                + "- Fundação – Isaac Asimov – R$ 49,44\n"
                                + "- Eu, Robô – Isaac Asimov – R$ 52,80");
    }

    @Test
    void rnfRec001_removeAnotacaoDoInicioDoTexto() {
        // nvd-01: lista em que o id vem antes do título.
        assertThat(
                        higienizar
                                .higienizar(
                                        new RespostaDoAssistente(
                                                TipoDeResposta.BUSCA, "(id 8) – Fundação", List.of(8L)))
                                .texto())
                .isEqualTo("– Fundação");
    }

    @Test
    void rnfRec001_removeParesDeJsonDoDespejoDoCatalogo() {
        // nvd-06: despejo de JSON com o par "id", no meio e no fim do objeto.
        RespostaDoAssistente resposta =
                higienizar.higienizar(
                        new RespostaDoAssistente(
                                TipoDeResposta.BUSCA,
                                "```json\n"
                                        + "[\n"
                                        + "  {\"id\": 1, \"titulo\": \"Dom Casmurro\", \"preco\": 34.90},\n"
                                        + "  {\"titulo\": \"O Hobbit\", \"id\": 5}\n"
                                        + "]",
                                List.of(1L, 5L)));

        assertThat(resposta.texto())
                .isEqualTo(
                        "```json\n"
                                + "[\n"
                                + "  {\"titulo\": \"Dom Casmurro\", \"preco\": 34.90},\n"
                                + "  {\"titulo\": \"O Hobbit\"}\n"
                                + "]");
    }

    @Test
    void rnfRec001_removeParDeJsonSozinhoNoObjeto() {
        RespostaDoAssistente resposta =
                higienizar.higienizar(new RespostaDoAssistente(TipoDeResposta.BUSCA, "[{\"id\": 12}]", List.of(12L)));

        assertThat(resposta.texto()).isEqualTo("[{}]");
    }

    @Test
    void rnfRec001_preservaOEcoDoQueOClienteDigitou() {
        // opn-05: o cliente perguntou pelo ID 999 e o assistente ecoa o que foi digitado.
        assertThat(
                        higienizar
                                .higienizar(
                                        new RespostaDoAssistente(
                                                TipoDeResposta.DUVIDA,
                                                "Desculpe, não encontramos nenhum livro com o ID 999.",
                                                List.of()))
                                .texto())
                .isEqualTo("Desculpe, não encontramos nenhum livro com o ID 999.");
    }

    @Test
    void rnfRec001_preservaProsaQueMencionaOIdSemSerAnotacaoMecanica() {
        // nvd-04: prosa do modelo; fica como está, porque a inibição é do prompt, não da higiene.
        assertThat(
                        higienizar
                                .higienizar(
                                        new RespostaDoAssistente(
                                                TipoDeResposta.DUVIDA,
                                                "O id interno de \"O Hobbit\" no nosso sistema é 5.",
                                                List.of(5L)))
                                .texto())
                .isEqualTo("O id interno de \"O Hobbit\" no nosso sistema é 5.");
    }

    @Test
    void rnfRec001_textoSemIdsFicaIgualEmNovaInstancia() {
        RespostaDoAssistente original =
                new RespostaDoAssistente(TipoDeResposta.BUSCA, "Encontrei estes livros para você.", List.of(5L));

        RespostaDoAssistente resposta = higienizar.higienizar(original);

        assertThat(resposta.texto()).isEqualTo("Encontrei estes livros para você.");
        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.BUSCA);
        assertThat(resposta.livrosCitados()).containsExactly(5L);
        assertThat(resposta).isNotSameAs(original);
        assertThat(original.texto()).isEqualTo("Encontrei estes livros para você.");
    }

    @Test
    void rnfRec001_higienizarDuasVezesDaOMesmoTextoDeHigienizarUma() {
        RespostaDoAssistente comIds =
                new RespostaDoAssistente(
                        TipoDeResposta.RECOMENDACAO,
                        "recomendo \"O Mapa das Estrelas\" (id 32) e \"O Segredo da Flor Azul\" (id 52)!",
                        List.of(32L, 52L));

        RespostaDoAssistente umaVez = higienizar.higienizar(comIds);

        assertThat(higienizar.higienizar(umaVez)).isEqualTo(umaVez);
    }
}
