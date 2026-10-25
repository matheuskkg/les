package br.fatec.les.livraria.recomendacao.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.CadeiaDeProvedores;
import jakarta.servlet.http.Cookie;

/**
 * Aba "Assistente" do ADMIN sobre o seed (RNF-REC-001, DEC-03): a cadeia tem gemini, groq, openrouter e fake, só o
 * fake com chave nos testes. As chamadas de outros provedores entram por SQL, dentro da transação do teste; a
 * recomendação de Bruno (usuário 3, sem compras) é atendida pelo fake.
 */
@SpringBootTest(properties = {
        "livraria.ia.provedores.gemini.chave=",
        "livraria.ia.provedores.groq.chave=",
        "livraria.ia.provedores.openrouter.chave="})
@Transactional
class IaAdminIntegracaoTest {

    private static final Long ADMIN = 1L;
    private static final Long ANA = 2L;
    private static final Long BRUNO = 3L;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 25, 12, 0);

    @TestConfiguration
    static class RelogioFixo {

        @Bean
        @Primary
        Relogio relogioFixo() {
            return () -> AGORA;
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

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        cadeia.limpar();
    }

    @AfterEach
    void limparCadeia() {
        // O cache da cadeia fica em memória e guardaria a ordem alterada depois do rollback.
        cadeia.limpar();
    }

    @AfterTransaction
    void limparChamadas() {
        // As chamadas da cadeia são gravadas em transação própria e não somem com o rollback do teste.
        JdbcClient.create(dataSource).sql("delete from chamada_ia").update();
    }

    private MockHttpServletRequestBuilder como(Long usuarioId, MockHttpServletRequestBuilder requisicao) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return requisicao.cookie(new Cookie(EmissorDeToken.COOKIE, token)).with(csrf());
    }

    private void registrarChamada(long id, long provedor, LocalDateTime em, String resultado, Integer codigoHttp,
            String erro) {
        JdbcClient.create(dataSource).sql("insert into chamada_ia (id, provedor_ia_id, modelo, operacao, "
                        + "iniciada_em, latencia_ms, tentativa, resultado, status_http, erro) "
                        + "values (?, ?, 'modelo', 'CONVERSAR', ?, 500, 1, ?, ?, ?)")
                .params(id, provedor, em, resultado, codigoHttp, erro)
                .update();
    }

    private String alterar(String corpo) throws Exception {
        return mvc.perform(como(ADMIN, put("/api/admin/ia/provedores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void rnfRec001_listaACadeiaComChaveConfiguradaSemExporChaveNemUrl() throws Exception {
        registrarChamada(900001, 1, AGORA.minusDays(2), "COTA_ESGOTADA", 429,
                "Falha em https://generativelanguage.googleapis.com/v1beta/openai");
        registrarChamada(900002, 1, AGORA.minusDays(1), "ERRO_HTTP", 500,
                "Falha em https://generativelanguage.googleapis.com/v1beta/openai");
        registrarChamada(900003, 1, AGORA, "SUCESSO", null, null);

        String corpo = mvc.perform(como(ADMIN, get("/api/admin/ia/provedores")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(org.hamcrest.Matchers.contains(
                        "gemini", "groq", "openrouter", "fake")))
                .andExpect(jsonPath("$[0].chaveConfigurada").value(false))
                .andExpect(jsonPath("$[0].modelo").isString())
                .andExpect(jsonPath("$[0].ultimaFalha.resultado").value("ERRO_HTTP"))
                .andExpect(jsonPath("$[0].ultimaFalha.codigoHttp").value(500))
                .andExpect(jsonPath("$[1].ultimaFalha").doesNotExist())
                .andExpect(jsonPath("$[3].chaveConfigurada").value(true))
                .andExpect(jsonPath("$[3].ativo").value(true))
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo).doesNotContain("googleapis", "groq.com", "openrouter.ai", "chave\"", "url");
    }

    @Test
    void rnfRec001_reordenaEDesativaJaNaProximaChamada() throws Exception {
        // Lê a cadeia antes, para o cache da cadeia já estar cheio quando o ADMIN alterar.
        mvc.perform(como(BRUNO, get("/api/me/recomendacoes")))
                .andExpect(jsonPath("$[0].justificativa").value("Entre os mais vendidos da loja."));

        String corpo = alterar("{\"provedores\":[{\"id\":4,\"ativo\":false},{\"id\":3,\"ativo\":true},"
                + "{\"id\":2,\"ativo\":true},{\"id\":1,\"ativo\":false}]}");

        assertThat(JsonPath.<List<String>>read(corpo, "$[*].nome")).containsExactly("fake", "openrouter", "groq",
                "gemini");
        assertThat(JsonPath.<List<Integer>>read(corpo, "$[*].ordem")).containsExactly(1, 2, 3, 4);
        assertThat(JsonPath.<List<Boolean>>read(corpo, "$[*].ativo")).containsExactly(false, true, true, false);

        // Sem o fake, nenhum provedor atende: a vitrine é gravada sem IA, sem justificativa.
        mvc.perform(como(ANA, get("/api/me/recomendacoes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].justificativa").doesNotExist());
    }

    @Test
    void rnfRec001_recusaCadeiaIncompletaRepetidaOuSemAtivacao() throws Exception {
        mvc.perform(como(ADMIN, put("/api/admin/ia/provedores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provedores\":[{\"id\":1,\"ativo\":true},{\"id\":1,\"ativo\":true}]}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].mensagem").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "Cada provedor aparece uma única vez na cadeia.", "Informe todos os provedores da cadeia.")));

        mvc.perform(como(ADMIN, put("/api/admin/ia/provedores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provedores\":[{\"id\":1},{\"id\":2,\"ativo\":true},{\"id\":3,\"ativo\":true},"
                                + "{\"id\":4,\"ativo\":true}]}"))
                .andExpect(status().isBadRequest());

        mvc.perform(como(ADMIN, put("/api/admin/ia/provedores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rnfRec001_metricasPorProvedorComAprovacaoESerieDiaria() throws Exception {
        registrarChamada(900001, 1, AGORA.minusDays(1), "COTA_ESGOTADA", 429, null);
        registrarChamada(900002, 1, AGORA.minusDays(1).plusHours(1), "COTA_ESGOTADA", 429, null);
        registrarChamada(900003, 1, AGORA.minusDays(30), "ERRO_HTTP", 500, null);
        JdbcClient.create(dataSource).sql("insert into recomendacao (id, cliente_id, gerada_em, "
                        + "compras_consideradas) values (900001, 1, ?, 0)")
                .param(AGORA.minusDays(2))
                .update();

        String vitrine = mvc.perform(como(BRUNO, get("/api/me/recomendacoes")))
                .andReturn().getResponse().getContentAsString();
        List<Integer> itens = JsonPath.read(vitrine, "$[*].id");
        for (int i = 0; i < 2; i++) {
            mvc.perform(como(BRUNO, put("/api/me/recomendacoes/itens/{id}/avaliacao", itens.get(i)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"avaliacao\":\"" + (i == 0 ? "POSITIVA" : "NEGATIVA") + "\"}"))
                    .andExpect(status().isNoContent());
        }

        mvc.perform(como(ADMIN, get("/api/admin/ia/metricas").param("inicio", "2026-10-21")
                        .param("fim", "2026-10-25")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inicio").value("2026-10-21"))
                .andExpect(jsonPath("$.provedores", hasSize(4)))
                .andExpect(jsonPath("$.provedores[0].nome").value("gemini"))
                .andExpect(jsonPath("$.provedores[0].chamadas").value(2))
                .andExpect(jsonPath("$.provedores[0].taxaDeSucesso").value(0.0))
                .andExpect(jsonPath("$.provedores[0].falhasPorResultado[0].resultado").value("COTA_ESGOTADA"))
                .andExpect(jsonPath("$.provedores[0].falhasPorResultado[0].quantidade").value(2))
                .andExpect(jsonPath("$.provedores[0].ultimaFalha.em").value("2026-10-24T13:00:00"))
                .andExpect(jsonPath("$.provedores[0].latenciaMediaMs").doesNotExist())
                .andExpect(jsonPath("$.provedores[1].chamadas").value(0))
                .andExpect(jsonPath("$.provedores[1].taxaDeSucesso").doesNotExist())
                .andExpect(jsonPath("$.provedores[3].nome").value("fake"))
                .andExpect(jsonPath("$.provedores[3].chamadas").value(1))
                .andExpect(jsonPath("$.provedores[3].taxaDeSucesso").value(1.0))
                .andExpect(jsonPath("$.provedores[3].latenciaP95Ms").isNumber())
                .andExpect(jsonPath("$.provedores[3].avaliadas").value(2))
                .andExpect(jsonPath("$.provedores[3].taxaDeAprovacao").value(0.5))
                .andExpect(jsonPath("$.recomendacoesSemIa").value(1))
                .andExpect(jsonPath("$.serie", hasSize(5)))
                .andExpect(jsonPath("$.serie[0].chamadas").value(0))
                .andExpect(jsonPath("$.serie[3].dia").value("2026-10-24"))
                .andExpect(jsonPath("$.serie[3].chamadas").value(2))
                .andExpect(jsonPath("$.serie[3].falhas").value(2))
                .andExpect(jsonPath("$.serie[4].chamadas").value(1))
                .andExpect(jsonPath("$.serie[4].falhas").value(0));
    }

    @Test
    void rnfRec001_periodoInvalidoDasMetricas() throws Exception {
        mvc.perform(como(ADMIN, get("/api/admin/ia/metricas").param("inicio", "2026-10-25")
                        .param("fim", "2026-10-24")))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(como(ADMIN, get("/api/admin/ia/metricas").param("inicio", "2025-01-01")
                        .param("fim", "2026-10-24")))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(como(ADMIN, get("/api/admin/ia/metricas").param("inicio", "25/10/2026")
                        .param("fim", "2026-10-24")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rnfRec001_clienteNaoAcessaAAbaDoAssistente() throws Exception {
        mvc.perform(como(ANA, get("/api/admin/ia/provedores"))).andExpect(status().isForbidden());
        mvc.perform(como(ANA, get("/api/admin/ia/metricas").param("inicio", "2026-10-01")
                .param("fim", "2026-10-25"))).andExpect(status().isForbidden());
    }
}
