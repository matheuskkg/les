package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.DetalheDoLivro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FalhaNaChamadaDeIa;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.LivroDoHistorico;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RespostaDoProvedor;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemAnterior;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;
import br.fatec.les.livraria.recomendacao.infraestrutura.PropriedadesIa;

/**
 * Adapter real contra um servidor HTTP local que imita a API OpenAI (RNF-REC-001, DEC-03): caminho e chave enviados,
 * saída estruturada, loop de tools controlado pelo adapter, formatos de resposta, limites de chamadas e a tradução de
 * cada falha para o resultado de {@code chamada_ia}.
 */
class AssistenteDeLivrosOpenAiTest {

    private static final String CHAVE = "chave-de-teste";
    private static final PerfilDoCliente PERFIL = new PerfilDoCliente(List.of(), List.of(), List.of(), List.of());
    private static final List<LivroCandidato> CANDIDATOS = List.of(
            new LivroCandidato(1L, "Dom Casmurro", List.of("Machado de Assis"), List.of("Romance"),
                    Dinheiro.de("39.90"), BigDecimal.ONE),
            new LivroCandidato(2L, "O Cortiço", List.of("Aluísio Azevedo"), List.of("Romance"),
                    Dinheiro.de("29.90"), BigDecimal.ONE));

    private HttpServer servidor;
    private final ConcurrentLinkedQueue<Resposta> respostas = new ConcurrentLinkedQueue<>();
    private final List<String> corpos = new CopyOnWriteArrayList<>();
    private final AtomicInteger requisicoes = new AtomicInteger();
    private volatile String caminhoRecebido;
    private volatile String autorizacaoRecebida;
    private volatile String tituloRecebido;
    private volatile String referenciaRecebida;

    private record Resposta(int status, String corpo, long atrasoMs) {
    }

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", this::atender);
        servidor.start();
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    private void atender(HttpExchange troca) throws IOException {
        requisicoes.incrementAndGet();
        caminhoRecebido = troca.getRequestURI().getPath();
        autorizacaoRecebida = troca.getRequestHeaders().getFirst("Authorization");
        tituloRecebido = troca.getRequestHeaders().getFirst("X-Title");
        referenciaRecebida = troca.getRequestHeaders().getFirst("HTTP-Referer");
        corpos.add(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        Resposta resposta = respostas.poll();
        if (resposta == null) {
            resposta = new Resposta(500, "{\"error\":{\"message\":\"sem resposta preparada\"}}", 0);
        }
        if (resposta.atrasoMs() > 0) {
            try {
                Thread.sleep(resposta.atrasoMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        byte[] corpo = resposta.corpo().getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().add("Content-Type", "application/json");
        try {
            troca.sendResponseHeaders(resposta.status(), corpo.length);
            try (OutputStream saida = troca.getResponseBody()) {
                saida.write(corpo);
            }
        } catch (IOException clienteDesistiu) {
            // O cliente fechou a conexão por tempo esgotado.
        }
    }

    private AssistenteDeLivrosOpenAi assistente(String urlBase, Duration tempoLimite) {
        return assistente(urlBase, tempoLimite, "instrucoes", "auto", 5);
    }

    private AssistenteDeLivrosOpenAi assistente() {
        return assistente("http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1", Duration.ofSeconds(5));
    }

    private AssistenteDeLivrosOpenAi assistente(String formato, String toolChoice, int maxChamadas) {
        return assistente("http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1", Duration.ofSeconds(5),
                formato, toolChoice, maxChamadas);
    }

    private AssistenteDeLivrosOpenAi assistente(String urlBase, Duration tempoLimite, String formato,
            String toolChoice, int maxChamadas) {
        return new AssistenteDeLivrosOpenAi("groq", provedor(urlBase, formato, toolChoice, maxChamadas), tempoLimite);
    }

    private static PropriedadesIa.Provedor provedor(String urlBase, String formato, String toolChoice) {
        return provedor(urlBase, formato, toolChoice, 5);
    }

    private static PropriedadesIa.Provedor provedor(String urlBase, String formato, String toolChoice,
            int maxChamadas) {
        return new PropriedadesIa.Provedor(urlBase, "modelo-teste", CHAVE, toolChoice, formato, Map.of(), maxChamadas);
    }

    private static String conclusao(String conteudoJson) {
        return conclusao(conteudoJson, 120, 30);
    }

    private static String conclusao(String conteudoJson, int tokensEntrada, int tokensSaida) {
        return """
                {"id":"chatcmpl-1","object":"chat.completion","created":1700000000,"model":"modelo-teste",
                 "choices":[{"index":0,"message":{"role":"assistant","content":%s},"finish_reason":"stop"}],
                 "usage":{"prompt_tokens":%d,"completion_tokens":%d,"total_tokens":%d}}"""
                .formatted(textoJson(conteudoJson), tokensEntrada, tokensSaida, tokensEntrada + tokensSaida);
    }

    private static String conclusaoSemUso(String conteudoJson) {
        return """
                {"id":"chatcmpl-1","object":"chat.completion","created":1700000000,"model":"modelo-teste",
                 "choices":[{"index":0,"message":{"role":"assistant","content":%s},"finish_reason":"stop"}]}"""
                .formatted(textoJson(conteudoJson));
    }

    private static String chamadaDeFerramenta() {
        return chamadaDeFerramenta("call_1");
    }

    private static String chamadaDeFerramenta(String id) {
        return respostaDeFerramentas("chatcmpl-" + id, 80, 10,
                ferramenta(id, "buscarLivros", "{\"texto\":\"machado\"}"));
    }

    private static String chamadaDeResposta(String tipo, String texto, String livrosCitadosJson) {
        return respostaDeFerramentas("chatcmpl-resposta", 120, 30,
                resposta(tipo, texto, livrosCitadosJson));
    }

    private static String resposta(String tipo, String texto, String livrosCitadosJson) {
        return ferramenta("call_r", "responder", "{\"tipo\":\"" + tipo + "\",\"texto\":" + textoJson(texto)
                + ",\"livrosCitados\":" + livrosCitadosJson + "}");
    }

    private static String respostaDeFerramentas(String id, int tokensEntrada, int tokensSaida,
            String... ferramentas) {
        return """
                {"id":"%s","object":"chat.completion","created":1700000000,"model":"modelo-teste",
                 "choices":[{"index":0,"message":{"role":"assistant","content":null,"tool_calls":[%s]},
                 "finish_reason":"tool_calls"}],
                 "usage":{"prompt_tokens":%d,"completion_tokens":%d,"total_tokens":%d}}"""
                .formatted(id, String.join(",", ferramentas), tokensEntrada, tokensSaida,
                        tokensEntrada + tokensSaida);
    }

    private static String ferramenta(String id, String nome, String argumentosJson) {
        return """
                {"id":"%s","type":"function","function":{"name":"%s","arguments":%s}}"""
                .formatted(id, nome, textoJson(argumentosJson));
    }

    private static String textoJson(String texto) {
        return "\"" + texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    @Test
    void rnfRec001_recomendarConverteASaidaEstruturadaComTokensNoCaminhoEChaveCertos() {
        respostas.add(new Resposta(200, conclusao("""
                {"itens":[{"livroId":2,"justificativa":"Naturalismo brasileiro."},
                          {"livroId":1,"justificativa":"Um clássico de Machado."}]}"""), 0));

        RespostaDoProvedor<RespostaDeRecomendacao> resposta = assistente().recomendar(PERFIL, CANDIDATOS);

        assertThat(caminhoRecebido).isEqualTo("/v1/chat/completions");
        assertThat(autorizacaoRecebida).isEqualTo("Bearer " + CHAVE);
        assertThat(resposta.conteudo().itens())
                .extracting(item -> item.livroId())
                .containsExactly(2L, 1L);
        assertThat(resposta.conteudo().itens().getFirst().justificativa()).isEqualTo("Naturalismo brasileiro.");
        assertThat(resposta.tokensEntrada()).isEqualTo(120);
        assertThat(resposta.tokensSaida()).isEqualTo(30);
        assertThat(resposta.chamadasHttp()).isEqualTo(1);
        assertThat(resposta.voltas()).singleElement().satisfies(volta -> {
            assertThat(volta.tokensEntrada()).isEqualTo(120);
            assertThat(volta.tokensSaida()).isEqualTo(30);
        });
        assertThat(corpos.getFirst()).contains("Dom Casmurro").contains("modelo-teste");
    }

    @Test
    void rnfRec001_recomendarCorrigeUmaVezARespostaInvalida() {
        respostas.add(new Resposta(200, conclusao(textoJson("Claro! Recomendo Dom Casmurro.")), 0));
        respostas.add(new Resposta(200, conclusao("""
                {"itens":[{"livroId":1,"justificativa":"Um clássico de Machado."}]}"""), 0));

        RespostaDoProvedor<RespostaDeRecomendacao> resposta = assistente().recomendar(PERFIL, CANDIDATOS);

        assertThat(resposta.conteudo().itens()).extracting(item -> item.livroId()).containsExactly(1L);
        assertThat(requisicoes).hasValue(2);
        assertThat(resposta.chamadasHttp()).isEqualTo(2);
        assertThat(corpos.get(1)).contains("Responda de novo somente com o JSON pedido.")
                .contains("Claro! Recomendo Dom Casmurro.");
    }

    @Test
    void rnfRec001_conversarExecutaAToolDeBuscaEConverteARespostaFinal() {
        respostas.add(new Resposta(200, chamadaDeFerramenta(), 0));
        respostas.add(new Resposta(200, conclusao("""
                {"tipo":"BUSCA","texto":"Encontrei Dom Casmurro.","livrosCitados":[1]}"""), 0));
        FerramentasFalsas ferramentas = new FerramentasFalsas();

        RespostaDoProvedor<RespostaDoAssistente> resposta = assistente().conversar(PERFIL,
                List.of(new MensagemAnterior(PapelDaMensagem.CLIENTE, "Oi"),
                        new MensagemAnterior(PapelDaMensagem.ASSISTENTE, "Olá! Como posso ajudar?")),
                "Livros do Machado?", ferramentas);

        assertThat(ferramentas.buscas).containsExactly("machado");
        assertThat(requisicoes).hasValue(2);
        assertThat(resposta.conteudo().tipo()).isEqualTo(TipoDeResposta.BUSCA);
        assertThat(resposta.conteudo().texto()).isEqualTo("Encontrei Dom Casmurro.");
        assertThat(resposta.conteudo().livrosCitados()).containsExactly(1L);
        assertThat(resposta.tokensEntrada()).isEqualTo(200);
        assertThat(resposta.tokensSaida()).isEqualTo(40);
        assertThat(resposta.chamadasHttp()).isEqualTo(2);
        assertThat(corpos.get(1)).contains("Dom Casmurro").contains("call_1");
    }

    @Test
    void rnfRec001_conversarComResponderEntregaARespostaSemOutraChamada() {
        respostas.add(new Resposta(200, chamadaDeResposta("BUSCA", "Encontrei Dom Casmurro.", "[1]"), 0));
        FerramentasFalsas ferramentas = new FerramentasFalsas();

        RespostaDoProvedor<RespostaDoAssistente> resposta = assistente("ferramenta", "auto", 5)
                .conversar(PERFIL, List.of(), "Livros do Machado?", ferramentas);

        assertThat(resposta.conteudo().tipo()).isEqualTo(TipoDeResposta.BUSCA);
        assertThat(resposta.conteudo().texto()).isEqualTo("Encontrei Dom Casmurro.");
        assertThat(resposta.conteudo().livrosCitados()).containsExactly(1L);
        assertThat(ferramentas.buscas).isEmpty();
        assertThat(requisicoes).hasValue(1);
        assertThat(resposta.chamadasHttp()).isEqualTo(1);
        assertThat(corpos.getFirst()).contains("\"tool_choice\":\"required\"")
                .contains("\"responder\"")
                .doesNotContain("\"response_format\"");
    }

    @Test
    void rnfRec001_responderVenceQuandoVemJuntoDeToolDeLeitura() {
        respostas.add(new Resposta(200, respostaDeFerramentas("chatcmpl-juntas", 80, 10,
                resposta("DUVIDA", "O frete é grátis acima de R$ 100.", "[]"),
                ferramenta("call_1", "buscarLivros", "{\"texto\":\"machado\"}")), 0));
        FerramentasFalsas ferramentas = new FerramentasFalsas();

        RespostaDoProvedor<RespostaDoAssistente> resposta = assistente("ferramenta", "auto", 5)
                .conversar(PERFIL, List.of(), "Como funciona o frete?", ferramentas);

        assertThat(resposta.conteudo().texto()).isEqualTo("O frete é grátis acima de R$ 100.");
        assertThat(ferramentas.buscas).isEmpty();
        assertThat(requisicoes).hasValue(1);
        assertThat(resposta.chamadasHttp()).isEqualTo(1);
    }

    @Test
    void rnfRec001_responderComArgumentosInvalidosRecebeUmaCorrecao() {
        respostas.add(new Resposta(200, respostaDeFerramentas("chatcmpl-invalida", 80, 10,
                ferramenta("call_r", "responder",
                        "{\"tipo\":\"NAO_EXISTE\",\"texto\":\"Oi\",\"livrosCitados\":[]}")), 0));
        respostas.add(new Resposta(200, chamadaDeResposta("DUVIDA", "Oi! Como posso ajudar?", "[]"), 0));

        RespostaDoProvedor<RespostaDoAssistente> resposta = assistente("ferramenta", "auto", 5)
                .conversar(PERFIL, List.of(), "Oi", new FerramentasFalsas());

        assertThat(resposta.conteudo().tipo()).isEqualTo(TipoDeResposta.DUVIDA);
        assertThat(resposta.conteudo().texto()).isEqualTo("Oi! Como posso ajudar?");
        assertThat(requisicoes).hasValue(2);
        assertThat(resposta.chamadasHttp()).isEqualTo(2);
        assertThat(corpos.get(1)).contains("call_r", "Erro:", "responder");
    }

    @Test
    void rnfRec001_ferramentaDesconhecidaRecebeCorrecaoEAceitaResponder() {
        respostas.add(new Resposta(200, respostaDeFerramentas("chatcmpl-desconhecida", 80, 10,
                ferramenta("call_x", "ferramentaQueNaoExiste", "{}")), 0));
        respostas.add(new Resposta(200, chamadaDeResposta("DUVIDA", "Oi! Como posso ajudar?", "[]"), 0));
        FerramentasFalsas ferramentas = new FerramentasFalsas();

        RespostaDoProvedor<RespostaDoAssistente> resposta = assistente("ferramenta", "auto", 5)
                .conversar(PERFIL, List.of(), "Oi", ferramentas);

        assertThat(resposta.conteudo().tipo()).isEqualTo(TipoDeResposta.DUVIDA);
        assertThat(requisicoes).hasValue(2);
        assertThat(resposta.chamadasHttp()).isEqualTo(2);
        assertThat(ferramentas.buscas).isEmpty();
        assertThat(corpos.get(1)).contains("ferramentaQueNaoExiste", "buscarLivros", "responder");
    }

    @Test
    void rnfRec001_ferramentaDesconhecidaDuasVezesViraRespostaInvalida() {
        respostas.add(new Resposta(200, respostaDeFerramentas("chatcmpl-desconhecida-1", 80, 10,
                ferramenta("call_x", "ferramentaQueNaoExiste", "{}")), 0));
        respostas.add(new Resposta(200, respostaDeFerramentas("chatcmpl-desconhecida-2", 80, 10,
                ferramenta("call_y", "outraFerramentaQueNaoExiste", "{}")), 0));

        assertThatThrownBy(() -> assistente("ferramenta", "auto", 5)
                .conversar(PERFIL, List.of(), "Oi", new FerramentasFalsas()))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha -> {
                    assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.RESPOSTA_INVALIDA);
                    assertThat(falha.getChamadasHttp()).isEqualTo(2);
                });
        assertThat(requisicoes).hasValue(2);
    }

    @Test
    void rnfRec001_duasRespostasInvalidasNoChatViramRespostaInvalida() {
        respostas.add(new Resposta(200, conclusao("{\"texto\":\"Oi\",\"livrosCitados\":[]}"), 0));
        respostas.add(new Resposta(200, conclusao("{\"texto\":\"Oi\",\"livrosCitados\":[]}"), 0));

        assertThatThrownBy(() -> assistente().conversar(PERFIL, List.of(), "Oi", new FerramentasFalsas()))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha -> {
                    assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.RESPOSTA_INVALIDA);
                    assertThat(falha.getChamadasHttp()).isEqualTo(2);
                });
        assertThat(requisicoes).hasValue(2);
    }

    @Test
    void rnfRec001_quatroVoltasDeToolsViramLimiteDeChamadasSemAQuintaChamada() {
        for (int volta = 1; volta <= 4; volta++) {
            respostas.add(new Resposta(200, chamadaDeFerramenta("call_" + volta), 0));
        }
        FerramentasFalsas ferramentas = new FerramentasFalsas();

        assertThatThrownBy(() -> assistente().conversar(PERFIL, List.of(), "Livros do Machado?", ferramentas))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha -> {
                    assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.LIMITE_DE_CHAMADAS);
                    assertThat(falha.getChamadasHttp()).isEqualTo(4);
                    assertThat(falha.getTokensEntrada()).isEqualTo(320);
                    assertThat(falha.getTokensSaida()).isEqualTo(40);
                });
        assertThat(requisicoes).hasValue(4);
        assertThat(ferramentas.buscas).hasSize(3);
    }

    @Test
    void rnfRec001_maxChamadasDoisNaoExecutaAToolDaSegundaResposta() {
        respostas.add(new Resposta(200, chamadaDeFerramenta("call_1"), 0));
        respostas.add(new Resposta(200, chamadaDeFerramenta("call_2"), 0));
        FerramentasFalsas ferramentas = new FerramentasFalsas();

        assertThatThrownBy(() -> assistente("instrucoes", "auto", 2)
                .conversar(PERFIL, List.of(), "Livros do Machado?", ferramentas))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha -> {
                    assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.LIMITE_DE_CHAMADAS);
                    assertThat(falha.getChamadasHttp()).isEqualTo(2);
                });
        assertThat(requisicoes).hasValue(2);
        assertThat(ferramentas.buscas).hasSize(1);
    }

    @Test
    void rnfRec001_falhaDepoisDeUmaVoltaDeToolsCarregaAsVoltasJaFeitas() {
        respostas.add(new Resposta(200, chamadaDeFerramenta(), 0));
        respostas.add(new Resposta(429, "{\"error\":{\"message\":\"Rate limit reached\"}}", 0));

        assertThatThrownBy(() -> assistente().conversar(PERFIL, List.of(), "Livros do Machado?",
                new FerramentasFalsas()))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha -> {
                    assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.COTA_ESGOTADA);
                    assertThat(falha.getChamadasHttp()).isEqualTo(2);
                    assertThat(falha.getTokensEntrada()).isEqualTo(80);
                    assertThat(falha.getTokensSaida()).isEqualTo(10);
                    assertThat(falha.getVoltas().get(1).tokensEntrada()).isNull();
                    assertThat(falha.getVoltas().get(1).tokensSaida()).isNull();
                });
        assertThat(requisicoes).hasValue(2);
    }

    @Test
    void rnfRec001_ferramentaUsaToolChoiceRequiredEmTodasAsVoltas() {
        respostas.add(new Resposta(200, chamadaDeFerramenta(), 0));
        respostas.add(new Resposta(200, chamadaDeResposta("BUSCA", "Encontrei Dom Casmurro.", "[1]"), 0));

        assistente("ferramenta", "auto", 5).conversar(PERFIL, List.of(), "Livros do Machado?",
                new FerramentasFalsas());

        assertThat(corpos.get(0)).contains("\"tool_choice\":\"required\"");
        assertThat(corpos.get(1)).contains("\"tool_choice\":\"required\"").contains("call_1");
    }

    @Test
    void rnfRec001_http429ViraCotaEsgotadaSemRepetirAChamada() {
        respostas.add(new Resposta(429, "{\"error\":{\"message\":\"Rate limit reached\"}}", 0));

        assertThatThrownBy(() -> assistente().recomendar(PERFIL, CANDIDATOS))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha -> {
                    assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.COTA_ESGOTADA);
                    assertThat(falha.getCodigoHttp()).isEqualTo(429);
                    assertThat(falha.getMessage()).doesNotContain(CHAVE);
                    assertThat(falha.getChamadasHttp()).isEqualTo(1);
                    assertThat(falha.getTokensEntrada()).isNull();
                });
        assertThat(requisicoes).hasValue(1);
    }

    @Test
    void rnfRec001_http500ViraErroHttp() {
        respostas.add(new Resposta(500, "{\"error\":{\"message\":\"Internal error\"}}", 0));

        assertThatThrownBy(() -> assistente().recomendar(PERFIL, CANDIDATOS))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha -> {
                    assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.ERRO_HTTP);
                    assertThat(falha.getCodigoHttp()).isEqualTo(500);
                });
        assertThat(requisicoes).hasValue(1);
    }

    @Test
    void rnfRec001_respostaLentaViraTempoEsgotado() {
        respostas.add(new Resposta(200, conclusao("{\"itens\":[]}"), 3_000));
        AssistenteDeLivrosOpenAi assistente = assistente(
                "http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1", Duration.ofSeconds(1));

        assertThatThrownBy(() -> assistente.recomendar(PERFIL, CANDIDATOS))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha ->
                        assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.TEMPO_ESGOTADO));
    }

    @Test
    void rnfRec001_conteudoQueNaoEhJsonViraRespostaInvalidaDepoisDaCorrecao() {
        respostas.add(new Resposta(200, conclusao("Claro! Recomendo Dom Casmurro."), 0));
        respostas.add(new Resposta(200, conclusao("Claro! Recomendo Dom Casmurro."), 0));

        assertThatThrownBy(() -> assistente().recomendar(PERFIL, CANDIDATOS))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha -> {
                    assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.RESPOSTA_INVALIDA);
                    assertThat(falha.getChamadasHttp()).isEqualTo(2);
                });
        assertThat(requisicoes).hasValue(2);
    }

    @Test
    void rnfRec001_tipoDesconhecidoViraRespostaInvalidaDepoisDaCorrecao() {
        respostas.add(new Resposta(200, conclusao("{\"texto\":\"Oi\",\"livrosCitados\":[]}"), 0));
        respostas.add(new Resposta(200, conclusao("{\"texto\":\"Oi\",\"livrosCitados\":[]}"), 0));

        assertThatThrownBy(() -> assistente().conversar(PERFIL, List.of(), "Oi", new FerramentasFalsas()))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha ->
                        assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.RESPOSTA_INVALIDA));
    }

    @Test
    void rnfRec001_servidorForaDoArViraErroDeRede() throws IOException {
        int portaFechada;
        try (ServerSocket socket = new ServerSocket(0)) {
            portaFechada = socket.getLocalPort();
        }
        AssistenteDeLivrosOpenAi assistente = assistente("http://127.0.0.1:" + portaFechada + "/v1",
                Duration.ofSeconds(2));

        assertThatThrownBy(() -> assistente.recomendar(PERFIL, CANDIDATOS))
                .isInstanceOfSatisfying(FalhaNaChamadaDeIa.class, falha ->
                        assertThat(falha.getResultado()).isEqualTo(ResultadoDaChamada.ERRO_DE_REDE));
    }

    @Test
    void rnfRec001_semChaveFicaForaDaCadeia() {
        AssistenteDeLivrosOpenAi assistente = new AssistenteDeLivrosOpenAi("gemini",
                new PropriedadesIa.Provedor("http://127.0.0.1:1/v1", "modelo-teste", "", "auto", "ferramenta",
                        Map.of(), 5),
                Duration.ofSeconds(1));

        assertThat(assistente.temChave()).isFalse();
        assertThat(assistente.nome()).isEqualTo("gemini");
        assertThat(assistente.modelo()).isEqualTo("modelo-teste");
        assertThat(assistente.maxChamadas()).isEqualTo(5);
    }

    @Test
    void rnfRec001_cabecalhosExtrasSaoEnviadosAoProvedor() {
        respostas.add(new Resposta(200, conclusao("{\"itens\":[]}"), 0));
        PropriedadesIa.Provedor provedor = new PropriedadesIa.Provedor(
                "http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1", "modelo-teste", CHAVE, "auto",
                "instrucoes", Map.of("X-Title", "Livraria LES", "HTTP-Referer", "https://livraria.test"), 5);

        new AssistenteDeLivrosOpenAi("openrouter", provedor, Duration.ofSeconds(5))
                .recomendar(PERFIL, CANDIDATOS);

        assertThat(tituloRecebido).isEqualTo("Livraria LES");
        assertThat(referenciaRecebida).isEqualTo("https://livraria.test");
    }

    @Test
    void rnfRec001_toolChoiceRequiredApareceSoNaPrimeiraVoltaDosFormatosDeTexto() {
        respostas.add(new Resposta(200, chamadaDeFerramenta(), 0));
        respostas.add(new Resposta(200, conclusao("""
                {"tipo":"BUSCA","texto":"Encontrei Dom Casmurro.","livrosCitados":[1]}"""), 0));
        AssistenteDeLivrosOpenAi assistente = new AssistenteDeLivrosOpenAi("groq",
                provedor("http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1", "instrucoes", "required"),
                Duration.ofSeconds(5));

        assistente.conversar(PERFIL, List.of(), "Livros do Machado?", new FerramentasFalsas());

        assertThat(corpos.get(0)).contains("\"tool_choice\":\"required\"");
        assertThat(corpos.get(1)).contains("\"tool_choice\":\"auto\"");
    }

    @Test
    void rnfRec001_toolChoiceRequiredNaoSeRepeteNaCorrecaoDosFormatosDeTexto() {
        respostas.add(new Resposta(200, conclusao("Claro! O frete é grátis acima de R$ 100."), 0));
        respostas.add(new Resposta(200, conclusao("""
                {"tipo":"DUVIDA","texto":"O frete é grátis acima de R$ 100.","livrosCitados":[]}"""), 0));
        AssistenteDeLivrosOpenAi assistente = new AssistenteDeLivrosOpenAi("groq",
                provedor("http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1", "instrucoes", "required"),
                Duration.ofSeconds(5));

        assistente.conversar(PERFIL, List.of(), "Como funciona o frete?", new FerramentasFalsas());

        assertThat(corpos.get(0)).contains("\"tool_choice\":\"required\"");
        assertThat(corpos.get(1)).contains("\"tool_choice\":\"auto\"");
    }

    @Test
    void rnfRec001_formatoJsonSchemaUsaOResponseFormatNativoSemInstrucoesNoPrompt() {
        respostas.add(new Resposta(200, conclusao("""
                {"itens":[{"livroId":1,"justificativa":"Um clássico de Machado."}]}"""), 0));
        AssistenteDeLivrosOpenAi assistente = new AssistenteDeLivrosOpenAi("openrouter",
                provedor("http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1", "json-schema", "auto"),
                Duration.ofSeconds(5));

        RespostaDoProvedor<RespostaDeRecomendacao> resposta = assistente.recomendar(PERFIL, CANDIDATOS);

        assertThat(resposta.conteudo().itens()).extracting(item -> item.livroId()).containsExactly(1L);
        assertThat(corpos.getFirst())
                .contains("\"response_format\"")
                .contains("\"type\":\"json_schema\"")
                .doesNotContain("Your response should be in JSON format")
                .doesNotContain("Here is the JSON Schema instance");
    }

    @Test
    void rnfRec001_formatoJsonSchemaNoChatNaoAnunciaAResponder() {
        respostas.add(new Resposta(200, conclusao("""
                {"tipo":"DUVIDA","texto":"O frete é grátis acima de R$ 100.","livrosCitados":[]}"""), 0));

        RespostaDoProvedor<RespostaDoAssistente> resposta = assistente("json-schema", "auto", 5)
                .conversar(PERFIL, List.of(), "Como funciona o frete?", new FerramentasFalsas());

        assertThat(resposta.conteudo().tipo()).isEqualTo(TipoDeResposta.DUVIDA);
        assertThat(corpos.getFirst()).contains("\"response_format\"").doesNotContain("\"responder\"");
    }

    @Test
    void rnfRec001_formatoJsonSchemaNaoAceitaResponderNaoAnunciado() {
        respostas.add(new Resposta(200, chamadaDeResposta("DUVIDA", "Oi", "[]"), 0));
        respostas.add(new Resposta(200, conclusao("""
                {"tipo":"DUVIDA","texto":"Oi! Como posso ajudar?","livrosCitados":[]}"""), 0));

        RespostaDoProvedor<RespostaDoAssistente> resposta = assistente("json-schema", "auto", 5)
                .conversar(PERFIL, List.of(), "Oi", new FerramentasFalsas());

        assertThat(resposta.conteudo().texto()).isEqualTo("Oi! Como posso ajudar?");
        assertThat(requisicoes).hasValue(2);
    }

    @Test
    void rnfRec001_respostaSemUsoMantemOsTokensNulos() {
        respostas.add(new Resposta(200, conclusaoSemUso("{\"itens\":[]}"), 0));

        RespostaDoProvedor<RespostaDeRecomendacao> resposta = assistente().recomendar(PERFIL, CANDIDATOS);

        assertThat(resposta.tokensEntrada()).isNull();
        assertThat(resposta.tokensSaida()).isNull();
        assertThat(resposta.chamadasHttp()).isEqualTo(1);
        assertThat(resposta.voltas()).singleElement().satisfies(volta -> {
            assertThat(volta.tokensEntrada()).isNull();
            assertThat(volta.tokensSaida()).isNull();
        });
    }

    @Test
    void rnfRec001_maxChamadasInvalidoEhRejeitado() {
        assertThatThrownBy(() -> new PropriedadesIa.Provedor(
                "http://127.0.0.1:1/v1", "modelo-teste", CHAVE, "auto", "ferramenta", Map.of(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("livraria.ia.provedores.*.max-chamadas deve ser pelo menos 1.");
    }

    private static final class FerramentasFalsas implements FerramentasDoAssistente {

        private final List<String> buscas = new ArrayList<>();

        @Override
        public List<ResumoDeLivro> buscarLivros(String texto, String categoria, BigDecimal precoMinimo,
                BigDecimal precoMaximo) {
            buscas.add(texto);
            return List.of(new ResumoDeLivro(1L, "Dom Casmurro", List.of("Machado de Assis"), List.of("Romance"),
                    Dinheiro.de("39.90")));
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
            return List.of();
        }
    }
}
