package br.fatec.les.livraria.clientes.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import jakarta.servlet.http.Cookie;

/**
 * Área "minha conta" do cliente sobre o seed 201_clientes.sql: consulta e alteração dos dados (RF-CLI-002), troca de
 * senha (RF-CLI-008) e inativação com reativação no login em até 7 dias (RF-CLI-003, DEC-10). Ana é o usuário 2 e
 * Bruno o usuário 3; a senha de todos é {@code Senha@123}.
 */
@SpringBootTest
@Transactional
class MinhaContaIntegracaoTest {

    private static final String SENHA = "Senha@123";
    private static final String NOVA_SENHA = "Nova@Senha9";
    private static final long ANA = 2L;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

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

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfCli002_clienteConsultaOsProprios() throws Exception {
        mvc.perform(get("/api/me").cookie(sessaoDe(ANA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.email").value("ana.souza@livraria.test"))
                .andExpect(jsonPath("$.telefone.ddd").value("11"));
    }

    @Test
    void dec08_minhaContaExigeSessaoDeCliente() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me").cookie(sessaoDe(1L))).andExpect(status().isForbidden());
    }

    @Test
    void rfCli002_alteraOsDadosEOContatoSemMexerNoResto() throws Exception {
        escrever(put("/api/me"), ANA, dados("Ana Souza Lima", "ana.lima@livraria.test", "988887011"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza Lima"))
                .andExpect(jsonPath("$.email").value("ana.lima@livraria.test"));

        mvc.perform(get("/api/me").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$.nome").value("Ana Souza Lima"))
                .andExpect(jsonPath("$.telefone.numero").value("988887011"));
        entrar("ana.lima@livraria.test", SENHA).andExpect(status().isOk());
    }

    @Test
    void rfCli002_manterOProprioEmailNaoViolaAUnicidade() throws Exception {
        escrever(put("/api/me"), ANA, dados("Ana Souza", "ana.souza@livraria.test", "988887001"))
                .andExpect(status().isOk());
    }

    @Test
    void rfCli002_emailETelefoneDeOutraContaEmUsoRespondem422() throws Exception {
        escrever(put("/api/me"), ANA, dados("Ana Souza", "bruno.lima@livraria.test", "988887001"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RF-CLI-002")));
    }

    @Test
    void rfCli008_senhaAtualErradaResponde422() throws Exception {
        escrever(post("/api/me/senha"), ANA, senha("Errada@123", NOVA_SENHA, NOVA_SENHA))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void rfCli008_rnfCli001_novaSenhaFracaOuDiferenteDaConfirmacaoResponde422() throws Exception {
        escrever(post("/api/me/senha"), ANA, senha(SENHA, "fraca", "outra"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RNF-CLI-001")))
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RNF-CLI-002")));
    }

    @Test
    void rfCli008_trocaASenhaEMantemASessao() throws Exception {
        Cookie sessao = sessaoDe(ANA);
        escrever(post("/api/me/senha"), sessao, senha(SENHA, NOVA_SENHA, NOVA_SENHA))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/me").cookie(sessao)).andExpect(status().isOk());
        entrar("ana.souza@livraria.test", SENHA).andExpect(status().isUnauthorized());
        entrar("ana.souza@livraria.test", NOVA_SENHA).andExpect(status().isOk());
    }

    @Test
    void rfCli003_inativarEncerraASessaoEOLoginEmAte7DiasReativa() throws Exception {
        Cookie sessao = sessaoDe(ANA);
        MvcResult inativacao = escrever(post("/api/me/inativar"), sessao, "")
                .andExpect(status().isNoContent())
                .andReturn();
        assertThat(inativacao.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie).startsWith(EmissorDeToken.COOKIE + "=")
                        .contains("Max-Age=0"));
        assertThat(usuarios.buscarPorId(ANA).orElseThrow().getInativadoEm()).isEqualTo(AGORA);

        mvc.perform(get("/api/me").cookie(sessao)).andExpect(status().isUnauthorized());

        Cookie novaSessao = entrar("ana.souza@livraria.test", SENHA)
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie(EmissorDeToken.COOKIE);
        assertThat(novaSessao).isNotNull();
        mvc.perform(get("/api/me").cookie(novaSessao)).andExpect(status().isOk());
        assertThat(usuarios.buscarPorId(ANA).orElseThrow().getInativadoEm()).isNull();
    }

    private ResultActions escrever(MockHttpServletRequestBuilder requisicao, long usuarioId, String corpo)
            throws Exception {
        return escrever(requisicao, sessaoDe(usuarioId), corpo);
    }

    private ResultActions escrever(MockHttpServletRequestBuilder requisicao, Cookie sessao, String corpo)
            throws Exception {
        Cookie csrf = tokenCsrf();
        return mvc.perform(requisicao.cookie(sessao, csrf).header("X-XSRF-TOKEN", csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions entrar(String login, String senha) throws Exception {
        Cookie csrf = tokenCsrf();
        return mvc.perform(post("/api/sessao").cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(login, senha)));
    }

    /** GET /api/sessao emite o cookie CSRF mesmo sem sessão (DEC-08). */
    private Cookie tokenCsrf() throws Exception {
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrf).isNotNull();
        return csrf;
    }

    private Cookie sessaoDe(long usuarioId) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }

    private static String dados(String nome, String email, String numero) {
        return """
                {
                  "nome": "%s",
                  "genero": "FEMININO",
                  "dataNascimento": "1990-03-14",
                  "cpf": "52998224725",
                  "email": "%s",
                  "telefone": { "tipo": "CELULAR", "ddd": "11", "numero": "%s" }
                }
                """.formatted(nome, email, numero);
    }

    private static String senha(String atual, String nova, String confirmacao) {
        return """
                { "senhaAtual": "%s", "novaSenha": "%s", "confirmacaoSenha": "%s" }
                """.formatted(atual, nova, confirmacao);
    }
}
