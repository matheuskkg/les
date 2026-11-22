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

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

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

import com.jayway.jsonpath.JsonPath;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.aplicacao.AssistenteIndisponivel;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.CadeiaDeProvedores;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ConversasRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.calculo.ValidarRespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarComprasDoCliente;
import jakarta.servlet.http.Cookie;

/**
 * Chat do assistente sobre o seed, atendido pelo provedor {@code fake} (RNF-REC-001, DEC-03). Ana (usuário 2) e
 * Bruno (usuário 3) são clientes ativos.
 */
@SpringBootTest(properties = {
        "livraria.ia.provedores.gemini.chave=",
        "livraria.ia.provedores.groq.chave=",
        "livraria.ia.provedores.openrouter.chave=",
        "livraria.ia.provedores.nvidia.chave="})
@Transactional
class AssistenteIntegracaoTest {

    private static final Long ANA = 2L;
    private static final Long BRUNO = 3L;
    private static final Long CLIENTE_DA_ANA = 1L;
    private static final Long O_HOBBIT = 5L;
    private static final AtomicReference<LocalDateTime> AGORA = new AtomicReference<>();

    @TestConfiguration
    static class RelogioAjustavel {

        @Bean
        @Primary
        Relogio relogioAjustavel() {
            return AGORA::get;
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CadeiaDeProvedores cadeia;

    @Autowired
    private ConversasRepositorio conversas;

    @Autowired
    private ConsultarComprasDoCliente comprasDoCliente;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        AGORA.set(LocalDateTime.of(2026, 10, 25, 12, 0));
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
        return mvc.perform(como(usuarioId, post("/api/me/assistente/mensagens"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":\"" + texto + "\"}"));
    }

    private Integer idDaResposta(Long usuarioId, String texto) throws Exception {
        String corpo = enviar(usuarioId, texto).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private ResultActions avaliar(Long usuarioId, Object mensagemId, String avaliacao) throws Exception {
        return mvc.perform(como(usuarioId, put("/api/me/assistente/mensagens/{id}/avaliacao", mensagemId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"avaliacao\":\"" + avaliacao + "\"}"));
    }

    /** Copia linhas de uma tabela trocando as colunas informadas por expressões SQL (pares coluna, expressão). */
    private void copiar(String tabela, String filtro, String... trocas) {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        List<String> colunas = jdbc.sql("select column_name from information_schema.columns "
                + "where table_schema = 'public' and table_name = :tabela order by ordinal_position")
                .param("tabela", tabela).query(String.class).list();
        java.util.Map<String, String> expressoes = new java.util.HashMap<>();
        for (int i = 0; i < trocas.length; i += 2) {
            expressoes.put(trocas[i], trocas[i + 1]);
        }
        String destino = colunas.stream().map(coluna -> "\"" + coluna + "\"")
                .collect(java.util.stream.Collectors.joining(", "));
        String origem = colunas.stream().map(coluna -> expressoes.getOrDefault(coluna, "\"" + coluna + "\""))
                .collect(java.util.stream.Collectors.joining(", "));
        jdbc.sql("insert into \"" + tabela + "\" (" + destino + ") select " + origem + " from \"" + tabela
                + "\" where " + filtro).update();
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
        List<Integer> comprados = comprasDoCliente.executar(CLIENTE_DA_ANA).stream()
                .map(vendido -> Math.toIntExact(vendido.livroId()))
                .distinct()
                .toList();

        String corpo = enviar(ANA, "Com base no que eu já comprei, me recomenda um livro")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("RECOMENDACAO"))
                .andReturn().getResponse().getContentAsString();

        List<Integer> citados = JsonPath.read(corpo, "$.livros[*].id");
        assertThat(citados).isNotEmpty().doesNotContainAnyElementsOf(comprados);
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
        String conversa = mvc.perform(como(ANA, get("/api/me/assistente/conversa")))
                .andReturn().getResponse().getContentAsString();
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

    /**
     * Fora da transação do teste: a mensagem do cliente precisa estar de fato gravada (commit) depois do 503. Os
     * dados gravados são apagados no fim.
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

            assertThat(jdbc.sql("select m.texto from mensagem_conversa m join conversa c on c.id = m.conversa_id "
                    + "where c.cliente_id = :cliente").param("cliente", CLIENTE_DA_ANA).query(String.class).list())
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
            for (String tabela : List.of("mensagem_livro_aud", "mensagem_conversa_aud", "conversa_aud",
                    "mensagem_livro", "mensagem_conversa", "conversa")) {
                jdbc.sql("delete from " + tabela).update();
            }
            jdbc.sql("delete from chamada_ia").update();
        }
    }
}
