package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.DetalheDoLivro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.LivroDoHistorico;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RespostaDoProvedor;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

/**
 * Comportamento determinístico do fake, sem rede (RNF-REC-001, DEC-03). O fake atende os testes e os E2E e é o
 * último provedor da cadeia.
 */
class AssistenteDeLivrosFakeTest {

    private static final PerfilDoCliente SEM_HISTORICO =
            new PerfilDoCliente(List.of(), List.of(), List.of(), List.of());
    private static final PerfilDoCliente COM_HISTORICO = new PerfilDoCliente(
            List.of(new LivroComprado(10L, "Memórias Póstumas", List.of("Romance"), List.of("Machado de Assis"))),
            List.of("Romance"),
            List.of("Machado de Assis"),
            List.of());

    private final AssistenteDeLivrosFake assistente = new AssistenteDeLivrosFake();
    private final FerramentasFalsas ferramentas = new FerramentasFalsas();

    @Test
    void rnfRec001_fakeTemNomeModeloEChave() {
        assertThat(assistente.nome()).isEqualTo("fake");
        assertThat(assistente.modelo()).isEqualTo("fake");
        assertThat(assistente.temChave()).isTrue();
    }

    @Test
    void rnfRec001_foraDoEscopoComFutebol() {
        RespostaDoAssistente resposta = conversar("Qual foi o resultado do futebol ontem?");

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.FORA_DO_ESCOPO);
        assertThat(resposta.livrosCitados()).isEmpty();
        assertThat(ferramentas.buscas()).isEmpty();
    }

    @Test
    void rnfRec001_foraDoEscopoComPrevisaoDoTempoSemDiferenciarAcento() {
        RespostaDoAssistente resposta = conversar("Como fica a previsão do tempo amanhã?");

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.FORA_DO_ESCOPO);
        assertThat(ferramentas.buscas()).isEmpty();
    }

    @Test
    void rnfRec001_buscaComAMensagemSemPontuacao() {
        ferramentas.comResultado("Dom Casmurro", livro(1L, "Dom Casmurro"));

        RespostaDoAssistente resposta = conversar("Dom Casmurro?");

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.BUSCA);
        assertThat(resposta.texto()).isEqualTo(AssistenteDeLivrosFake.TEXTO_DE_BUSCA);
        assertThat(resposta.livrosCitados()).containsExactly(1L);
        assertThat(ferramentas.buscas()).containsExactly("Dom Casmurro");
    }

    @Test
    void rnfRec001_buscaTentaCadaPalavraDeQuatroLetrasAteAcharResultado() {
        ferramentas.comResultado("Machado", livro(2L, "Dom Casmurro"));

        RespostaDoAssistente resposta = conversar("Quero ler Machado");

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.BUSCA);
        assertThat(resposta.livrosCitados()).containsExactly(2L);
        // "ler" tem três letras e não é tentada.
        assertThat(ferramentas.buscas()).containsExactly("Quero ler Machado", "Quero", "Machado");
    }

    @Test
    void rnfRec001_mensagemSoDePontuacaoNaoBuscaOCatalogoInteiro() {
        RespostaDoAssistente resposta = conversar("???");

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.DUVIDA);
        assertThat(ferramentas.buscas()).isEmpty();
    }

    @Test
    void rnfRec001_semResultadoRespondeDuvida() {
        RespostaDoAssistente resposta = conversar("Livro raro");

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.DUVIDA);
        assertThat(resposta.texto()).isEqualTo(AssistenteDeLivrosFake.TEXTO_DE_DUVIDA);
        assertThat(resposta.livrosCitados()).isEmpty();
        assertThat(ferramentas.buscas()).containsExactly("Livro raro", "Livro", "raro");
    }

    @Test
    void rnfRec001_buscaLimitaAteTresLivrosCitados() {
        ferramentas.comResultado("Romance",
                livro(1L, "Dom Casmurro"),
                livro(2L, "Memórias Póstumas"),
                livro(3L, "Quincas Borba"),
                livro(4L, "A Cartomante"));

        RespostaDoAssistente resposta = conversar("Romance");

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.BUSCA);
        assertThat(resposta.livrosCitados()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void rnfRec001_pedidoDeRecomendacaoUsaOsCandidatosDoPerfil() {
        ferramentas.comRecomendados(livro(7L, "O Cortiço"), livro(8L, "Capitães da Areia"));

        RespostaDoAssistente resposta = conversar("Com base no que eu já comprei, me recomenda um livro");

        assertThat(resposta.tipo()).isEqualTo(TipoDeResposta.RECOMENDACAO);
        assertThat(resposta.texto()).isEqualTo(AssistenteDeLivrosFake.TEXTO_DE_RECOMENDACAO);
        assertThat(resposta.livrosCitados()).containsExactly(7L, 8L);
        assertThat(ferramentas.pedidosDeRecomendacao()).isEqualTo(1);
        assertThat(ferramentas.buscas()).isEmpty();
    }

    @Test
    void rnfRec001_recomendarMantemAOrdemRecebidaComJustificativaDoPerfil() {
        List<LivroCandidato> candidatos = List.of(candidato(3L, "Quincas Borba"), candidato(1L, "Dom Casmurro"));

        RespostaDeRecomendacao resposta = assistente.recomendar(COM_HISTORICO, candidatos).conteudo();

        assertThat(resposta.itens()).extracting(item -> item.livroId()).containsExactly(3L, 1L);
        assertThat(resposta.itens()).extracting(item -> item.justificativa())
                .containsOnly(AssistenteDeLivrosFake.JUSTIFICATIVA_DO_PERFIL);
    }

    @Test
    void rnfRec001_recomendarSemHistoricoUsaOsMaisVendidos() {
        List<LivroCandidato> candidatos = List.of(candidato(1L, "Dom Casmurro"));

        RespostaDeRecomendacao resposta = assistente.recomendar(SEM_HISTORICO, candidatos).conteudo();

        assertThat(resposta.itens()).extracting(item -> item.livroId()).containsExactly(1L);
        assertThat(resposta.itens()).extracting(item -> item.justificativa())
                .containsOnly(AssistenteDeLivrosFake.JUSTIFICATIVA_MAIS_VENDIDOS);
    }

    private RespostaDoAssistente conversar(String mensagem) {
        RespostaDoProvedor<RespostaDoAssistente> resposta =
                assistente.conversar(SEM_HISTORICO, List.of(), mensagem, ferramentas, List.of());
        assertThat(resposta.tokensEntrada()).isNull();
        assertThat(resposta.tokensSaida()).isNull();
        return resposta.conteudo();
    }

    private static LivroCandidato candidato(Long id, String titulo) {
        return new LivroCandidato(id, titulo, List.of("Machado de Assis"), List.of("Romance"),
                Dinheiro.de("29.90"), new BigDecimal("10"));
    }

    private static ResumoDeLivro livro(Long id, String titulo) {
        return new ResumoDeLivro(id, titulo, List.of("Machado de Assis"), List.of("Romance"),
                Dinheiro.de("29.90"));
    }

    /** Tools em memória, com os resultados indexados pelo texto buscado. */
    private static final class FerramentasFalsas implements FerramentasDoAssistente {

        private final Map<String, List<ResumoDeLivro>> resultados = new HashMap<>();
        private final List<String> buscas = new ArrayList<>();
        private List<ResumoDeLivro> recomendados = List.of();
        private int pedidosDeRecomendacao;

        FerramentasFalsas comResultado(String texto, ResumoDeLivro... livros) {
            resultados.put(texto, List.of(livros));
            return this;
        }

        FerramentasFalsas comRecomendados(ResumoDeLivro... livros) {
            recomendados = List.of(livros);
            return this;
        }

        List<String> buscas() {
            return buscas;
        }

        int pedidosDeRecomendacao() {
            return pedidosDeRecomendacao;
        }

        @Override
        public List<ResumoDeLivro> buscarLivros(String texto, String categoria, BigDecimal precoMinimo,
                BigDecimal precoMaximo) {
            buscas.add(texto);
            return resultados.getOrDefault(texto, List.of());
        }

        @Override
        public Optional<DetalheDoLivro> verLivro(Long livroId) {
            return Optional.empty();
        }

        @Override
        public List<LivroDoHistorico> verMeuHistorico() {
            return List.of();
        }

        @Override
        public List<ResumoDeLivro> recomendarParaMim() {
            pedidosDeRecomendacao++;
            return recomendados;
        }
    }
}
