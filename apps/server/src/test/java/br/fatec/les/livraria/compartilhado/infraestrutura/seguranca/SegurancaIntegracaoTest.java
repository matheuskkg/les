package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.TokenDeSessao;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;

/** Regras de autenticação, autorização e CSRF com o contexto real da aplicação (DEC-08, DEC-15.4). */
@SpringBootTest
class SegurancaIntegracaoTest {

    @RestController
    static class ControllerDeSeguranca {

        @GetMapping("/api/livros/exemplo")
        String livros() {
            return "livros";
        }

        @GetMapping("/api/me/exemplo")
        String meu() {
            return "me";
        }

        @GetMapping("/api/admin/exemplo")
        String admin() {
            return "admin";
        }

        @PostMapping("/api/exemplo/escrita")
        String escrita() {
            return "escrita";
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        ControllerDeSeguranca controllerDeSeguranca() {
            return new ControllerDeSeguranca();
        }
    }

    @Autowired private WebApplicationContext contexto;

    @Autowired private EmissorDeToken emissor;

    @Autowired private JwtDecoder jwtDecoder;

    @Autowired private UsuarioRepositorio usuarios;

    @Autowired private CodificadorDeSenha senhas;

    @Autowired private Relogio relogio;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void getPublicoDaVitrineEhAcessivelSemSessaoEEmiteOTokenCsrf() throws Exception {
        mvc.perform(get("/api/livros/exemplo"))
                .andExpect(status().isOk())
                .andExpect(content().string("livros"))
                .andExpect(resultado -> {
                    Cookie csrf = resultado.getResponse().getCookie("XSRF-TOKEN");
                    assertThat(csrf).isNotNull();
                    assertThat(csrf.isHttpOnly()).isFalse();
                });
    }

    @Test
    void rotaDoClienteSemSessaoResponde401EmProblemDetail() throws Exception {
        mvc.perform(get("/api/me/exemplo"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Autenticação necessária."));
    }

    @Test
    void rotaDeAdminComSessaoDeClienteResponde403EmProblemDetail() throws Exception {
        Usuario cliente = criarCliente("cliente.admin.403@livraria.test");

        mvc.perform(get("/api/admin/exemplo").cookie(cookieDe(cliente)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Acesso negado."));
    }

    @Test
    void rotaDeAdminComSessaoDoAdminDoSeedResponde200() throws Exception {
        mvc.perform(get("/api/admin/exemplo").cookie(cookieDe(administrador())))
                .andExpect(status().isOk())
                .andExpect(content().string("admin"));
    }

    @Test
    void rotaDoClienteComSessaoDeClienteResponde200() throws Exception {
        Usuario cliente = criarCliente("cliente.me.200@livraria.test");

        mvc.perform(get("/api/me/exemplo").cookie(cookieDe(cliente)))
                .andExpect(status().isOk())
                .andExpect(content().string("me"));
    }

    @Test
    void usuarioInativoNoBancoInvalidaOToken() throws Exception {
        Usuario inativo =
                usuarios.salvar(
                        new Usuario(
                                null,
                                "inativo.seguranca@livraria.test",
                                null,
                                senhas.codificar("Senha@123"),
                                Papel.CLIENTE,
                                relogio.agora().minusDays(1)));

        mvc.perform(get("/api/me/exemplo").cookie(cookieDe(inativo)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Autenticação necessária."));
    }

    @Test
    void cookieDeUsuarioInativoNaoBloqueiaARotaPublicaDaVitrine() throws Exception {
        Usuario inativo =
                usuarios.salvar(
                        new Usuario(
                                null,
                                "inativo.vitrine@livraria.test",
                                null,
                                senhas.codificar("Senha@123"),
                                Papel.CLIENTE,
                                relogio.agora().minusDays(1)));

        mvc.perform(get("/api/livros/exemplo").cookie(cookieDe(inativo)))
                .andExpect(status().isOk())
                .andExpect(content().string("livros"));
    }

    @Test
    void cookieInvalidoEmRotaDoClienteResponde401EApagaOCookie() throws Exception {
        Usuario inativo =
                usuarios.salvar(
                        new Usuario(
                                null,
                                "inativo.me.cookie@livraria.test",
                                null,
                                senhas.codificar("Senha@123"),
                                Papel.CLIENTE,
                                relogio.agora().minusDays(1)));

        mvc.perform(get("/api/me/exemplo").cookie(cookieDe(inativo)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Autenticação necessária."))
                .andExpect(resultado ->
                        assertThat(resultado.getResponse().getHeaders("Set-Cookie"))
                                .anyMatch(cookie ->
                                        cookie.startsWith(EmissorDeToken.COOKIE + "=")
                                                && cookie.contains("Max-Age=0")));
    }

    @Test
    void cookieComAssinaturaInvalidaEmRotaDoClienteResponde401EApagaOCookie() throws Exception {
        mvc.perform(get("/api/me/exemplo").cookie(new Cookie(EmissorDeToken.COOKIE, "token-invalido")))
                .andExpect(status().isUnauthorized())
                .andExpect(resultado ->
                        assertThat(resultado.getResponse().getHeaders("Set-Cookie"))
                                .anyMatch(cookie ->
                                        cookie.startsWith(EmissorDeToken.COOKIE + "=")
                                                && cookie.contains("Max-Age=0")));
    }

    @Test
    void usuarioInexistenteNoBancoInvalidaOToken() throws Exception {
        Usuario fantasma =
                new Usuario(999_999L, "fantasma.seguranca@livraria.test", null, "hash-ficticio", Papel.CLIENTE, null);

        mvc.perform(get("/api/me/exemplo").cookie(cookieDe(fantasma)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Autenticação necessária."));
    }

    @Test
    void papelVemDoBancoENaoDaClaimDoToken() throws Exception {
        Usuario cliente = criarCliente("cliente.claim.admin@livraria.test");
        Usuario tokenComPapelAdmin =
                new Usuario(cliente.getId(), cliente.getEmail(), null, cliente.getSenhaHash(), Papel.ADMIN, null);

        mvc.perform(get("/api/admin/exemplo").cookie(cookieDe(tokenComPapelAdmin)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Acesso negado."));
    }

    @Test
    void postSemTokenCsrfResponde403ComMensagemPropria() throws Exception {
        mvc.perform(post("/api/exemplo/escrita").cookie(cookieDe(administrador())))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Token CSRF ausente ou inválido."));
    }

    @Test
    void getDeRotaDaSpaForaDaApiEhPublico() throws Exception {
        mvc.perform(get("/painel/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("web de teste")));
    }

    @Test
    void tokenTemAsClaimsDoDec08() {
        TokenDeSessao token = emissor.emitir(administrador());

        Jwt jwt = jwtDecoder.decode(token.valor());

        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getClaimAsString("papel")).isEqualTo("ADMIN");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(EmissorDeToken.EMISSOR);
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }

    @Test
    void cookieDeSessaoTemOsAtributosDoDec08() {
        ResponseCookie cookie = emissor.emitir(administrador()).cookie();

        assertThat(cookie.getName()).isEqualTo(EmissorDeToken.COOKIE);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo("/api");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofHours(8));
        assertThat(cookie.isSecure()).isFalse();
        assertThat(emissor.cookieExpirado().getMaxAge()).isZero();
    }

    private Usuario administrador() {
        return usuarios.buscarPorId(1L).orElseThrow();
    }

    private Usuario criarCliente(String email) {
        return usuarios.salvar(new Usuario(null, email, null, senhas.codificar("Senha@123"), Papel.CLIENTE, null));
    }

    private Cookie cookieDe(Usuario usuario) {
        return new Cookie(EmissorDeToken.COOKIE, emissor.emitir(usuario).valor());
    }
}
