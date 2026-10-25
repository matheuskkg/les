package br.fatec.les.livraria.acesso.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.CredenciaisInvalidas;
import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.TokenDeSessao;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import jakarta.servlet.http.Cookie;

/** Rotas de sessão de ponta a ponta (DEC-08, DEC-10, DEC-15.4). */
@SpringBootTest
class SessaoIntegracaoTest {

    private static final String ADMIN = "admin@livraria.test";
    private static final String SENHA_DO_ADMIN = "Admin@123";
    private static final String SENHA_DE_TESTE = "Senha@123";
    private static final String COOKIE_CSRF = "XSRF-TOKEN";
    private static final String CABECALHO_CSRF = "X-XSRF-TOKEN";

    @RestController
    static class ControllerDoAdmin {

        @GetMapping("/api/admin/exemplo")
        String exemplo() {
            return "admin";
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        ControllerDoAdmin controllerDoAdmin() {
            return new ControllerDoAdmin();
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private CodificadorDeSenha senhas;

    @Autowired
    private Relogio relogio;

    @Autowired
    private EmissorDeToken emissor;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void loginDoAdminGravaOCookieDoDec08ERetornaOsDadosDaSessao() throws Exception {
        MockHttpServletResponse resposta = entrar(ADMIN, SENHA_DO_ADMIN)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.email").value(ADMIN))
                .andExpect(jsonPath("$.papel").value("ADMIN"))
                .andExpect(jsonPath("$.nome").value("Administração"))
                .andReturn().getResponse();

        String cookie = setCookieDaSessao(resposta);
        assertThat(cookie).contains("HttpOnly", "SameSite=Strict", "Path=/api");
        assertThat(resposta.getCookie(EmissorDeToken.COOKIE)).isNotNull();
    }

    @Test
    void cookieDoLoginAbreAGetDaSessaoEAUmaRotaDeAdmin() throws Exception {
        Cookie sessao = cookieDaSessao(entrar(ADMIN, SENHA_DO_ADMIN)
                .andExpect(status().isOk())
                .andReturn().getResponse());

        mvc.perform(get("/api/sessao").cookie(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.email").value(ADMIN))
                .andExpect(jsonPath("$.papel").value("ADMIN"))
                // A carga inicial da SPA consulta a sessão e precisa receber o token CSRF (DEC-08).
                .andExpect(resultado -> {
                    Cookie csrf = resultado.getResponse().getCookie(COOKIE_CSRF);
                    assertThat(csrf).isNotNull();
                    assertThat(csrf.isHttpOnly()).isFalse();
                });

        mvc.perform(get("/api/admin/exemplo").cookie(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string("admin"));
    }

    @Test
    void sessaoDoClienteTrazONomeDoCliente() throws Exception {
        Cookie sessao = cookieDaSessao(entrar("ana.souza@livraria.test", SENHA_DE_TESTE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andReturn().getResponse());

        mvc.perform(get("/api/sessao").cookie(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("CLIENTE"))
                .andExpect(jsonPath("$.nome").value("Ana Souza"));
    }

    @Test
    void senhaErradaResponde401ComAMensagemUnica() throws Exception {
        entrar(ADMIN, "SenhaErrada@1")
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value(CredenciaisInvalidas.MENSAGEM));
    }

    @Test
    void loginDesconhecidoRespondeOMesmo401() throws Exception {
        entrar("ninguem@livraria.test", SENHA_DE_TESTE)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(CredenciaisInvalidas.MENSAGEM));
    }

    @Test
    void loginSemTokenCsrfResponde403() throws Exception {
        mvc.perform(post("/api/sessao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoDeLogin(ADMIN, SENHA_DO_ADMIN)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Token CSRF ausente ou inválido."));
    }

    @Test
    void loginComCamposEmBrancoResponde400() throws Exception {
        comTokenCsrf(post("/api/sessao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoDeLogin("  ", "")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void loginReativaAContaInativadaDentroDoPrazo_RF_CLI_003() throws Exception {
        Usuario inativa = criarUsuario("reativa.sessao@livraria.test", relogio.agora().minusDays(6));

        entrar("reativa.sessao@livraria.test", SENHA_DE_TESTE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("CLIENTE"));

        assertThat(usuarios.buscarPorId(inativa.getId()))
                .get()
                .extracting(Usuario::getInativadoEm)
                .isNull();
    }

    @Test
    void loginDeContaEncerradaResponde401ComoDesconhecida_DEC_15_4() throws Exception {
        criarUsuario("encerrada.sessao@livraria.test", relogio.agora().minusDays(8));

        entrar("encerrada.sessao@livraria.test", SENHA_DE_TESTE)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(CredenciaisInvalidas.MENSAGEM));
    }

    @Test
    void logoutApagaOCookieDeSessao() throws Exception {
        Cookie csrf = tokenCsrf();
        Cookie sessao = cookieDaSessao(entrar(ADMIN, SENHA_DO_ADMIN, csrf)
                .andExpect(status().isOk())
                .andReturn().getResponse());

        MockHttpServletResponse resposta = mvc.perform(delete("/api/sessao")
                .cookie(sessao, csrf)
                .header(CABECALHO_CSRF, csrf.getValue()))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        assertThat(setCookieDaSessao(resposta)).contains("Max-Age=0", "Path=/api");
    }

    @Test
    void loginRotacionaOTokenCsrf() throws Exception {
        Cookie csrfAntes = tokenCsrf();

        MockHttpServletResponse resposta = entrar(ADMIN, SENHA_DO_ADMIN, csrfAntes)
                .andExpect(status().isOk())
                .andReturn().getResponse();

        String csrfDepois = valorDoCookie(resposta, COOKIE_CSRF);
        assertThat(csrfDepois).isNotBlank().isNotEqualTo(csrfAntes.getValue());
    }

    @Test
    void logoutRotacionaOTokenCsrf() throws Exception {
        MockHttpServletResponse login = entrar(ADMIN, SENHA_DO_ADMIN, tokenCsrf())
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Cookie sessao = cookieDaSessao(login);
        String csrfAntes = valorDoCookie(login, COOKIE_CSRF);

        MockHttpServletResponse resposta = mvc.perform(delete("/api/sessao")
                .cookie(sessao, new Cookie(COOKIE_CSRF, csrfAntes))
                .header(CABECALHO_CSRF, csrfAntes))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        assertThat(valorDoCookie(resposta, COOKIE_CSRF)).isNotBlank().isNotEqualTo(csrfAntes);
    }

    @Test
    void loginComCookieDeSessaoInvalidoUsaAsCredenciaisEReativaAConta() throws Exception {
        Usuario inativa = criarUsuario("reativa.cookie@livraria.test", relogio.agora().minusDays(1));
        Cookie cookieAntigo = cookieDe(inativa);
        Cookie csrf = tokenCsrf();

        mvc.perform(post("/api/sessao")
                .cookie(cookieAntigo, csrf)
                .header(CABECALHO_CSRF, csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoDeLogin("reativa.cookie@livraria.test", SENHA_DE_TESTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("CLIENTE"));

        assertThat(usuarios.buscarPorId(inativa.getId()))
                .get()
                .extracting(Usuario::getInativadoEm)
                .isNull();
    }

    @Test
    void logoutComCookieDeSessaoInvalidoResponde204EApagaOCookie() throws Exception {
        Usuario inativa = criarUsuario("inativa.cookie@livraria.test", relogio.agora().minusDays(1));
        Cookie csrf = tokenCsrf();

        MockHttpServletResponse resposta = mvc.perform(delete("/api/sessao")
                .cookie(cookieDe(inativa), csrf)
                .header(CABECALHO_CSRF, csrf.getValue()))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        assertThat(setCookieDaSessao(resposta)).contains("Max-Age=0");
    }

    @Test
    void getDaSessaoComCookieInvalidoResponde401EApagaOCookie() throws Exception {
        Usuario inativa = criarUsuario("inativa.consulta@livraria.test", relogio.agora().minusDays(1));

        MockHttpServletResponse resposta = mvc.perform(get("/api/sessao").cookie(cookieDe(inativa)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Autenticação necessária."))
                .andReturn().getResponse();

        assertThat(setCookieDaSessao(resposta)).contains("Max-Age=0");
    }

    @Test
    void getDaSessaoSemCookieResponde401EmProblemDetail() throws Exception {
        mvc.perform(get("/api/sessao"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Autenticação necessária."))
                .andExpect(resultado -> assertThat(resultado.getResponse().getCookie(COOKIE_CSRF)).isNotNull());
    }

    private ResultActions entrar(String login, String senha) throws Exception {
        return entrar(login, senha, tokenCsrf());
    }

    private ResultActions entrar(String login, String senha, Cookie csrf) throws Exception {
        return mvc.perform(post("/api/sessao")
                .cookie(csrf)
                .header(CABECALHO_CSRF, csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoDeLogin(login, senha)));
    }

    private ResultActions comTokenCsrf(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao)
            throws Exception {
        Cookie csrf = tokenCsrf();
        return mvc.perform(requisicao.cookie(csrf).header(CABECALHO_CSRF, csrf.getValue()));
    }

    /** GET /api/sessao emite o cookie CSRF mesmo sem sessão (DEC-08); ele vale para a próxima escrita. */
    private Cookie tokenCsrf() throws Exception {
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie(COOKIE_CSRF);
        assertThat(csrf).isNotNull();
        return csrf;
    }

    private Cookie cookieDe(Usuario usuario) {
        TokenDeSessao token = emissor.emitir(usuario);
        return new Cookie(EmissorDeToken.COOKIE, token.valor());
    }

    /** Último valor não vazio do cookie no response, ignorando o Set-Cookie de expiração da rotação. */
    private static String valorDoCookie(MockHttpServletResponse resposta, String nome) {
        return resposta.getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(valor -> valor.startsWith(nome + "="))
                .map(valor -> valor.substring((nome + "=").length()).split(";", 2)[0])
                .filter(valor -> !valor.isBlank())
                .reduce((anterior, atual) -> atual)
                .orElse(null);
    }

    private Usuario criarUsuario(String email, LocalDateTime inativadoEm) {
        return usuarios.salvar(new Usuario(null, email, null, senhas.codificar(SENHA_DE_TESTE), Papel.CLIENTE,
                inativadoEm));
    }

    private static Cookie cookieDaSessao(MockHttpServletResponse resposta) {
        return resposta.getCookie(EmissorDeToken.COOKIE);
    }

    private static String setCookieDaSessao(MockHttpServletResponse resposta) {
        Collection<String> cookies = resposta.getHeaders(HttpHeaders.SET_COOKIE);
        return cookies.stream()
                .filter(valor -> valor.startsWith(EmissorDeToken.COOKIE + "="))
                .findFirst()
                .orElseThrow();
    }

    private static String corpoDeLogin(String login, String senha) {
        return "{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(login, senha);
    }
}
