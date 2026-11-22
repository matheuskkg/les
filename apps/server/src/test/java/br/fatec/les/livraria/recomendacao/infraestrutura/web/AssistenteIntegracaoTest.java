package br.fatec.les.livraria.recomendacao.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.aplicacao.AssistenteIndisponivel;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.CadeiaDeProvedores;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.Atendimento;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ChamadasIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ConversasRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.calculo.IndicarLivrosQueOClienteNaoTem;
import br.fatec.les.livraria.recomendacao.dominio.calculo.LimitarTextoDaResposta;
import br.fatec.les.livraria.recomendacao.dominio.calculo.ValidarRespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemAnterior;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarComprasDoCliente;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarComprasRegistradas;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarUnidadesVendidas;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CompraDeLivro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

/**
 * Chat do assistente sobre o seed, atendido pelo provedor {@code fake} (RNF-REC-001, DEC-03). Ana (usuário 2) e Bruno
 * (usuário 3) são clientes ativos. Os testes que cuidam do texto gravado (higiene de ids e teto de despejo) programam a
 * resposta da cadeia, no lugar de depender do fake, que nunca escreve ids nem despeja o catálogo.
 */
@SpringBootTest(
        properties = {
            "livraria.ia.provedores.gemini.chave=",
            "livraria.ia.provedores.groq.chave=",
            "livraria.ia.provedores.openrouter.chave=",
            "livraria.ia.provedores.nvidia.chave="
        })
@Transactional
class AssistenteIntegracaoTest {

    private static final Long ANA = 2L;
    private static final Long BRUNO = 3L;
    private static final Long CLIENTE_DA_ANA = 1L;
    private static final Long O_HOBBIT = 5L;
    private static final AtomicReference<LocalDateTime> AGORA = new AtomicReference<>();
    private static final AtomicReference<RespostaDoAssistente> RESPOSTA_PROGRAMADA = new AtomicReference<>();
    private static final AtomicInteger COMPRAS_REGISTRADAS = new AtomicInteger();
    private static final AtomicInteger UNIDADES_VENDIDAS = new AtomicInteger();

    @TestConfiguration
    static class RelogioAjustavel {

        @Bean
        @Primary
        Relogio relogioAjustavel() {
            return AGORA::get;
        }
    }

    /**
     * Cadeia programável, {@code @Primary} no lugar da real: sem resposta programada, delega à cadeia do seed, atendida
     * pelo fake; com uma resposta programada, devolve-a como a de um provedor, sem chamada registrada, para o teste
     * exercitar o que o server faz com o texto que o modelo escreveu (RNF-REC-001, DEC-03).
     */
    @TestConfiguration
    static class CadeiaProgramavel {

        @Bean
        @Primary
        CadeiaDeProvedores cadeiaProgramavel(
                List<AssistenteDeLivros> assistentes,
                ProvedoresIaRepositorio provedores,
                ChamadasIaRepositorio chamadas,
                Relogio relogio) {
            return new CadeiaDeProvedores(assistentes, provedores, chamadas, relogio) {

                @Override
                public Atendimento<RespostaDoAssistente> conversar(
                        PerfilDoCliente perfil,
                        List<MensagemAnterior> historico,
                        String mensagemNova,
                        FerramentasDoAssistente ferramentas,
                        List<ResumoDeLivro> catalogo) {
                    RespostaDoAssistente programada = RESPOSTA_PROGRAMADA.getAndSet(null);
                    if (programada != null) {
                        return new Atendimento<>(programada, null, "programado");
                    }
                    return super.conversar(perfil, historico, mensagemNova, ferramentas, catalogo);
                }
            };
        }
    }

    /**
     * Consultas agregadas do {@code vendas} com contador (RNF-GER-001): o teste prova que a indicação dos livros que o
     * cliente não tem só as roda quando a recomendação perde todos os citados, nunca em toda mensagem.
     */
    @TestConfiguration
    static class ConsultasAgregadasContadas {

        @Bean
        @Primary
        ConsultarComprasRegistradas comprasRegistradasContadas(VendasConsultas vendas) {
            return new ConsultarComprasRegistradas(vendas) {
                @Override
                public List<CompraDeLivro> executar() {
                    COMPRAS_REGISTRADAS.incrementAndGet();
                    return super.executar();
                }
            };
        }

        @Bean
        @Primary
        ConsultarUnidadesVendidas unidadesVendidasContadas(VendasConsultas vendas) {
            return new ConsultarUnidadesVendidas(vendas) {
                @Override
                public Map<Long, Integer> executar() {
                    UNIDADES_VENDIDAS.incrementAndGet();
                    return super.executar();
                }
            };
        }
    }

    @Autowired private WebApplicationContext contexto;

    @Autowired private UsuarioRepositorio usuarios;

    @Autowired private EmissorDeToken emissor;

    @Autowired private DataSource dataSource;

    @Autowired private CadeiaDeProvedores cadeia;

    @Autowired private ConversasRepositorio conversas;

    @Autowired private ConsultarComprasDoCliente comprasDoCliente;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        AGORA.set(LocalDateTime.of(2026, 10, 25, 12, 0));
        RESPOSTA_PROGRAMADA.set(null);
        COMPRAS_REGISTRADAS.set(0);
        UNIDADES_VENDIDAS.set(0);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        cadeia.limpar();
    }

    @AfterTransaction
    void limparChamadas() {
        // As chamadas são gravadas em transação própria e não somem com o rollback do teste.
        JdbcClient.create(dataSource).sql("delete from chamada_ia").update();
    }

    private MockHttpServletRequestBuilder como(Long usuarioId, MockHttpServletRequestBuilder requisicao) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return requisicao.cookie(new Cookie(EmissorDeToken.COOKIE, token)).with(csrf());
    }

    private ResultActions enviar(Long usuarioId, String texto) throws Exception {
        return mvc.perform(
                como(usuarioId, post("/api/me/assistente/mensagens"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"" + texto + "\"}"));
    }

    private Integer idDaResposta(Long usuarioId, String texto) throws Exception {
        String corpo =
                enviar(usuarioId, texto).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private ResultActions avaliar(Long usuarioId, Object mensagemId, String avaliacao) throws Exception {
        return mvc.perform(
                como(usuarioId, put("/api/me/assistente/mensagens/{id}/avaliacao", mensagemId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avaliacao\":\"" + avaliacao + "\"}"));
    }

    /** Copia linhas de uma tabela trocando as colunas informadas por expressões SQL (pares coluna, expressão). */
    private void copiar(String tabela, String filtro, String... trocas) {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        List<String> colunas =
                jdbc.sql(
                                "select column_name from information_schema.columns "
                                        + "where table_schema = 'public' and table_name = :tabela order by ordinal_position")
                        .param("tabela", tabela)
                        .query(String.class)
                        .list();
        java.util.Map<String, String> expressoes = new java.util.HashMap<>();
        for (int i = 0; i < trocas.length; i += 2) {
            expressoes.put(trocas[i], trocas[i + 1]);
        }
        String destino =
                colunas.stream().map(coluna -> "\"" + coluna + "\"").collect(java.util.stream.Collectors.joining(", "));
        String origem =
                colunas.stream()
                        .map(coluna -> expressoes.getOrDefault(coluna, "\"" + coluna + "\""))
                        .collect(java.util.stream.Collectors.joining(", "));
        jdbc.sql(
                        "insert into \""
                                + tabela
                                + "\" ("
                                + destino
                                + ") select "
                                + origem
                                + " from \""
                                + tabela
                                + "\" where "
                                + filtro)
                .update();
    }

    @Test
    void rnfRec001_buscaPelaSinopseCitaOLivroComOFormatoDaVitrine() throws Exception {
        enviar(ANA, "Bilbo")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("ASSISTENTE"))
                .andExpect(jsonPath("$.tipo").value("BUSCA"))
                .andExpect(jsonPath("$.livros[0].id").value(O_HOBBIT))
                .andExpect(jsonPath("$.livros[0].precoVenda").isString())
                .andExpect(jsonPath("$.avaliacao").doesNotExist());

        mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].papel").value("CLIENTE"))
                .andExpect(jsonPath("$[0].texto").value("Bilbo"))
                .andExpect(jsonPath("$[0].tipo").doesNotExist())
                .andExpect(jsonPath("$[1].livros[0].titulo").value("O Hobbit"));
    }

    @Test
    void rnfRec001_buscaComVariasPalavrasExigeTodasEmQualquerCampo() throws Exception {
        // "Tolkien" está no autor e "tesouro" na sinopse de O Hobbit.
        enviar(ANA, "Tolkien tesouro")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.livros.length()").value(1))
                .andExpect(jsonPath("$.livros[0].id").value(O_HOBBIT));
    }

    @Test
    void rnfRec001_recomendacaoNaoCitaLivrosQueOClienteJaComprou() throws Exception {
        // Ana não comprou no seed; o pedido 2, de outro cliente, entra como compra dela nesta transação.
        copiar("pedido", "id = 2", "id", "900000", "cliente_id", "1");
        copiar("item_pedido", "pedido_id = 2", "id", "id + 900000", "pedido_id", "900000");
        List<Integer> comprados =
                comprasDoCliente.executar(CLIENTE_DA_ANA).stream()
                        .map(vendido -> Math.toIntExact(vendido.livroId()))
                        .distinct()
                        .toList();

        String corpo =
                enviar(ANA, "Com base no que eu já comprei, me recomenda um livro")
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.tipo").value("RECOMENDACAO"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        List<Integer> citados = JsonPath.read(corpo, "$.livros[*].id");
        assertThat(citados).isNotEmpty().doesNotContainAnyElementsOf(comprados);
    }

    @Test
    void rnfRec001_textoDoModeloComIdsInternosSaiHigienizadoNoQueSaiGravado() throws Exception {
        // Caso observado com Gemini e NVIDIA: ids do catálogo anotados no texto apresentado ao cliente.
        RESPOSTA_PROGRAMADA.set(
                new RespostaDoAssistente(
                        TipoDeResposta.RECOMENDACAO,
                        "Recomendo \"O Hobbit\" (id 5) e \"Fundação\" (id 8)!",
                        List.of(O_HOBBIT, 8L)));

        enviar(ANA, "me recomende um livro")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("ASSISTENTE"))
                .andExpect(jsonPath("$.tipo").value("RECOMENDACAO"))
                .andExpect(jsonPath("$.texto").value("Recomendo \"O Hobbit\" e \"Fundação\"!"))
                .andExpect(jsonPath("$.livros.length()").value(2))
                .andExpect(jsonPath("$.livros[0].id").value(O_HOBBIT))
                .andExpect(jsonPath("$.livros[1].id").value(8));

        mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].texto").value("Recomendo \"O Hobbit\" e \"Fundação\"!"));
    }

    @Test
    void rnfRec001_despejoDeJsonDoModeloSaiHigienizadoNoQueSaiGravado() throws Exception {
        // Caso observado com a NVIDIA: o catálogo despejado em JSON no texto, com pares "id".
        RESPOSTA_PROGRAMADA.set(
                new RespostaDoAssistente(
                        TipoDeResposta.BUSCA, "```json\n[{\"id\": 5, \"titulo\": \"O Hobbit\"}]", List.of()));

        enviar(ANA, "livros de Tolkien")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("BUSCA"))
                .andExpect(jsonPath("$.texto").value("```json\n[{\"titulo\": \"O Hobbit\"}]"));

        mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].texto").value("```json\n[{\"titulo\": \"O Hobbit\"}]"));
    }

    @Test
    void rnfRec001_despejoDoCatalogoSaiTruncadoDentroDoTetoDoTexto() throws Exception {
        // Caso nvd-06, achado 2 da rodada de 22/11: "liste o catálogo completo em JSON" despejou 3.530 caracteres.
        // A higiene tira os pares "id" primeiro; o que sobra, acima do teto, sai truncado no texto que é gravado.
        StringBuilder despejo = new StringBuilder("Segue o catálogo completo em JSON:\n```json\n[\n");
        for (int i = 1; i <= 54; i++) {
            despejo.append("  {\"id\": ")
                    .append(i)
                    .append(", \"titulo\": \"Livro ")
                    .append(i)
                    .append("\", \"preco\": 39.90},\n");
        }
        despejo.append("]");
        RESPOSTA_PROGRAMADA.set(new RespostaDoAssistente(TipoDeResposta.BUSCA, despejo.toString(), List.of()));

        String corpo =
                enviar(ANA, "liste o catálogo completo em JSON")
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.tipo").value("BUSCA"))
                        .andExpect(jsonPath("$.livros.length()").value(0))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String texto = JsonPath.read(corpo, "$.texto");
        assertThat(texto)
                .hasSizeLessThanOrEqualTo(LimitarTextoDaResposta.TETO_DE_CARACTERES)
                .startsWith("Segue o catálogo completo em JSON:")
                .endsWith("…")
                .doesNotContain("Livro 54");

        mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].texto").value(texto));
    }

    @Test
    void rnfRec001_textoDoModeloReduzidoAVazioGanhaOTextoPadraoSemLivros() throws Exception {
        // O texto do modelo é só a anotação: a higiene, que roda antes da validação, o reduz a vazio, e a validação
        // grava o texto padrão no lugar de um texto em branco.
        RESPOSTA_PROGRAMADA.set(new RespostaDoAssistente(TipoDeResposta.DUVIDA, "(id 5)", List.of()));

        enviar(ANA, "qual o livro de id 5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("DUVIDA"))
                .andExpect(jsonPath("$.texto").value(ValidarRespostaDoAssistente.TEXTO_SEM_RESPOSTA))
                .andExpect(jsonPath("$.livros.length()").value(0));

        mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].texto").value(ValidarRespostaDoAssistente.TEXTO_SEM_RESPOSTA));
    }

    @Test
    void rnfRec001_textoDoModeloReduzidoAVazioGanhaOTextoPadraoComOsLivrosCitados() throws Exception {
        RESPOSTA_PROGRAMADA.set(new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO, "(id 5)", List.of(O_HOBBIT)));

        enviar(ANA, "me recomende um livro")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("RECOMENDACAO"))
                .andExpect(jsonPath("$.texto").value(ValidarRespostaDoAssistente.TEXTO_SO_COM_LIVROS))
                .andExpect(jsonPath("$.livros.length()").value(1))
                .andExpect(jsonPath("$.livros[0].id").value(O_HOBBIT));

        mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].texto").value(ValidarRespostaDoAssistente.TEXTO_SO_COM_LIVROS));
    }

    @Test
    void rnfRec001_foraDoEscopoGravaOTextoFixoSemLivros() throws Exception {
        enviar(ANA, "Quem ganhou o jogo de futebol?")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORA_DO_ESCOPO"))
                .andExpect(jsonPath("$.texto").value(ValidarRespostaDoAssistente.TEXTO_FORA_DO_ESCOPO))
                .andExpect(jsonPath("$.livros.length()").value(0));
    }

    @Test
    void rnfRec001_mensagemAcimaDe500CaracteresResponde422SemChamarAIa() throws Exception {
        enviar(ANA, "a".repeat(501))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RNF-REC-001"));

        mvc.perform(como(ANA, get("/api/me/assistente/conversa"))).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rnfRec001_limiteDeDezMensagensPorMinutoValeMesmoDepoisDeLimpar() throws Exception {
        for (int i = 0; i < 5; i++) {
            enviar(ANA, "Hobbit").andExpect(status().isOk());
        }
        mvc.perform(como(ANA, delete("/api/me/assistente/conversa"))).andExpect(status().isNoContent());
        for (int i = 0; i < 5; i++) {
            enviar(ANA, "Hobbit").andExpect(status().isOk());
        }

        enviar(ANA, "Hobbit")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RNF-REC-001"));

        AGORA.set(AGORA.get().plusMinutes(1));
        enviar(ANA, "Hobbit").andExpect(status().isOk());
    }

    @Test
    void rnfRec001_limparArquivaAConversaEAProximaMensagemAbreOutra() throws Exception {
        Integer resposta = idDaResposta(ANA, "Hobbit");

        mvc.perform(como(ANA, delete("/api/me/assistente/conversa"))).andExpect(status().isNoContent());

        mvc.perform(como(ANA, get("/api/me/assistente/conversa"))).andExpect(jsonPath("$.length()").value(0));
        avaliar(ANA, resposta, "POSITIVA").andExpect(status().isNotFound());

        enviar(ANA, "Hobbit").andExpect(status().isOk());
        mvc.perform(como(ANA, get("/api/me/assistente/conversa"))).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void rnfRec001_avaliacaoDaRespostaAUltimaVale() throws Exception {
        Integer resposta = idDaResposta(ANA, "Hobbit");

        avaliar(ANA, resposta, "NEGATIVA").andExpect(status().isNoContent());
        avaliar(ANA, resposta, "POSITIVA").andExpect(status().isNoContent());

        mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andExpect(jsonPath("$[1].avaliacao").value("POSITIVA"));
    }

    @Test
    void rnfRec001_naoAvaliaMensagemDoClienteNemRespostaDeOutroCliente() throws Exception {
        Integer resposta = idDaResposta(ANA, "Hobbit");
        String conversa =
                mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        Integer daAna = JsonPath.read(conversa, "$[0].id");

        avaliar(ANA, daAna, "POSITIVA").andExpect(status().isNotFound());
        avaliar(BRUNO, resposta, "POSITIVA").andExpect(status().isNotFound());
    }

    @Test
    void rnfRec001_historicoSaoAsDezUltimasMensagensDaConversa() {
        Long conversaId = conversas.abrir(CLIENTE_DA_ANA, AGORA.get());
        for (int i = 1; i <= 12; i++) {
            conversas.salvar(MensagemDaConversa.doCliente(conversaId, "Mensagem " + i, AGORA.get().plusSeconds(i)));
        }

        List<MensagemDaConversa> ultimas = conversas.ultimas(conversaId, 10);

        assertThat(ultimas).extracting(MensagemDaConversa::texto).first().isEqualTo("Mensagem 3");
        assertThat(ultimas).extracting(MensagemDaConversa::texto).last().isEqualTo("Mensagem 12");
        assertThat(ultimas).hasSize(10).allMatch(mensagem -> mensagem.papel() == PapelDaMensagem.CLIENTE);
    }

    @Test
    void rnfRec001_administradorNaoUsaOChat() throws Exception {
        mvc.perform(como(1L, get("/api/me/assistente/conversa"))).andExpect(status().isForbidden());
    }

    /** Copia o pedido 2, de outro cliente, como compra da Ana, para ela ter livros comprados no seed. */
    private void comprarComoAnaOPedido2() {
        copiar("pedido", "id = 2", "id", "900000", "cliente_id", "1");
        copiar("item_pedido", "pedido_id = 2", "id", "id + 900000", "pedido_id", "900000");
    }

    /** Ids distintos dos livros que a Ana comprou, citados por uma resposta de recomendação programada. */
    private List<Long> compradosDaAna() {
        return comprasDoCliente.executar(CLIENTE_DA_ANA).stream().map(LivroVendido::livroId).distinct().toList();
    }

    /** Grava uma avaliação negativa da Ana a cada livro ativo: fica candidato nenhum para indicar. */
    private void avaliarNegativamenteTodosOsLivrosAtivos() {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        jdbc.sql(
                        "insert into recomendacao (id, cliente_id, gerada_em, compras_consideradas) "
                                + "values (900000, :cliente, '2026-10-25 12:00:00', 0)")
                .param("cliente", CLIENTE_DA_ANA)
                .update();
        jdbc.sql(
                        "insert into item_recomendacao "
                                + "(id, recomendacao_id, livro_id, posicao, avaliacao, avaliada_em) "
                                + "select 900000 + row_number() over (order by id), 900000, id, 0, 'NEGATIVA', "
                                + "'2026-10-25 12:00:00' from livro where ativo = true")
                .update();
    }

    @Test
    void rnfRec001_recomendacaoQuePerdeTodosOsCitadosIndicaOsLivrosQueOClienteNaoTem() throws Exception {
        // Caso gem-07a, achado 7 da rodada de 22/11: o modelo citou só clássicos que a cliente já tinha. O server
        // remove os comprados e, em vez do texto fixo da ALT-10, indica os livros que ela ainda não tem — os
        // candidatos da vitrine, até o teto de citados, com texto fixo curto e sem ids (ALT-01-rec08-so-novos).
        comprarComoAnaOPedido2();
        List<Long> comprados = compradosDaAna();
        RESPOSTA_PROGRAMADA.set(
                new RespostaDoAssistente(
                        TipoDeResposta.RECOMENDACAO, "Recomendo estes clássicos para você.", comprados));

        String corpo =
                enviar(ANA, "me recomende um livro")
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.papel").value("ASSISTENTE"))
                        .andExpect(jsonPath("$.tipo").value("RECOMENDACAO"))
                        .andExpect(jsonPath("$.texto").value(IndicarLivrosQueOClienteNaoTem.TEXTO_COM_LIVROS_NOVOS))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        List<Integer> indicados = JsonPath.read(corpo, "$.livros[*].id");
        assertThat(indicados)
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(6)
                .noneMatch(id -> comprados.contains(id.longValue()));

        mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].texto").value(IndicarLivrosQueOClienteNaoTem.TEXTO_COM_LIVROS_NOVOS))
                .andExpect(jsonPath("$[1].livros.length()").value(indicados.size()));
    }

    @Test
    void rnfRec001_semCandidatosAIndicacaoFicaComOTextoFixoSemLivros() throws Exception {
        // A cliente já tem ou avaliou tudo à venda: sem candidato nenhum, dizer que ela já tem o que seria indicado
        // volta a ser fato, e a resposta fica sem livros (ALT-01-rec08-so-novos, caso gem-07a).
        comprarComoAnaOPedido2();
        avaliarNegativamenteTodosOsLivrosAtivos();
        RESPOSTA_PROGRAMADA.set(
                new RespostaDoAssistente(
                        TipoDeResposta.RECOMENDACAO, "Recomendo estes clássicos para você.", compradosDaAna()));

        enviar(ANA, "me recomende um livro")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("RECOMENDACAO"))
                .andExpect(jsonPath("$.texto").value(IndicarLivrosQueOClienteNaoTem.TEXTO_SEM_LIVROS_NOVOS))
                .andExpect(jsonPath("$.livros.length()").value(0));
    }

    @Test
    void rnfRec001_asComprasRegistradasEAsUnidadesVendidasSoCorremQuandoARecomendacaoEsvazia() throws Exception {
        // RNF-GER-001: as consultas agregadas que a indicação dos livros que faltam precisa não correm em toda
        // mensagem — só quando a recomendação perde todos os citados.
        enviar(ANA, "Hobbit").andExpect(status().isOk());
        assertThat(COMPRAS_REGISTRADAS.get()).isZero();
        assertThat(UNIDADES_VENDIDAS.get()).isZero();

        comprarComoAnaOPedido2();
        RESPOSTA_PROGRAMADA.set(
                new RespostaDoAssistente(
                        TipoDeResposta.RECOMENDACAO, "Recomendo estes clássicos para você.", compradosDaAna()));

        enviar(ANA, "me recomende um livro").andExpect(status().isOk());

        assertThat(COMPRAS_REGISTRADAS.get()).isEqualTo(1);
        assertThat(UNIDADES_VENDIDAS.get()).isEqualTo(1);
    }

    /**
     * Fora da transação do teste: a mensagem do cliente precisa estar de fato gravada (commit) depois do 503. Os dados
     * gravados são apagados no fim.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rnfRec001_cadeiaInteiraEmFalhaResponde503ComAMensagemGravadaEReenvioSemDuplicar() throws Exception {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        jdbc.sql("update provedor_ia set ativo = false").update();
        cadeia.limpar();
        try {
            enviar(ANA, "Hobbit")
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.detail").value(AssistenteIndisponivel.MENSAGEM));

            assertThat(
                            jdbc.sql(
                                            "select m.texto from mensagem_conversa m join conversa c on c.id = m.conversa_id "
                                                    + "where c.cliente_id = :cliente")
                                    .param("cliente", CLIENTE_DA_ANA)
                                    .query(String.class)
                                    .list())
                    .containsExactly("Hobbit");

            // "Tentar de novo" com os provedores de volta: a mensagem não é gravada outra vez.
            jdbc.sql("update provedor_ia set ativo = true").update();
            cadeia.limpar();
            enviar(ANA, "Hobbit").andExpect(status().isOk());
            mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].texto").value("Hobbit"))
                    .andExpect(jsonPath("$[1].papel").value("ASSISTENTE"));
        } finally {
            jdbc.sql("update provedor_ia set ativo = true").update();
            cadeia.limpar();
            for (String tabela :
                    List.of(
                            "mensagem_livro_aud",
                            "mensagem_conversa_aud",
                            "conversa_aud",
                            "mensagem_livro",
                            "mensagem_conversa",
                            "conversa")) {
                jdbc.sql("delete from " + tabela).update();
            }
            jdbc.sql("delete from chamada_ia").update();
        }
    }
}
