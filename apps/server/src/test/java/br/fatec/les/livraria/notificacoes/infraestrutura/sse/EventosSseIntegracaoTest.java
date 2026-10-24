package br.fatec.les.livraria.notificacoes.infraestrutura.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.EncerrarCanais;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import jakarta.servlet.http.Cookie;

/**
 * Stream SSE das notificações (DEC-13): {@code text/event-stream}, entrega só depois do commit, reenvio pelo
 * {@code Last-Event-ID} e fechamento do canal no logout.
 */
@SpringBootTest
class EventosSseIntegracaoTest {

    private static final String SENHA = "Senha@123";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private CodificadorDeSenha senhas;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private Notificar notificar;

    @Autowired
    private EncerrarCanais encerrarCanais;

    @Autowired
    private RegistroDeEmissores registro;

    private MockMvc mvc;
    private Usuario cliente;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        cliente = usuarios.salvar(new Usuario(null, "sse." + System.nanoTime() + "@livraria.test", null,
                senhas.codificar(SENHA), Papel.CLIENTE, null));
    }

    @AfterEach
    void fecharCanais() {
        encerrarCanais.executar(cliente.getId());
    }

    @Test
    void dec13_abreOStreamComoTextEventStream() throws Exception {
        MvcResult resultado = abrirStream(null);

        assertThat(resultado.getResponse().getContentType())
                .startsWith(MediaType.TEXT_EVENT_STREAM_VALUE);
        assertThat(registro.contar(cliente.getId())).isEqualTo(1);
    }

    @Test
    void dec13_entregaANotificacaoPublicadaDepoisDoCommitComIdTipoEData() throws Exception {
        MvcResult resultado = abrirStream(null);

        Notificacao notificacao = notificar.executar(Notificar.Comando.sobre(cliente.getId(),
                TipoNotificacao.PEDIDO_APROVADO, "Seu pedido foi aprovado.", "pedido", 10042L));

        String corpo = corpo(resultado);
        assertThat(corpo)
                .contains("id:" + notificacao.getId())
                .contains("event:notificacao")
                .contains("\"tipo\":\"PEDIDO_APROVADO\"")
                .contains("data:{")
                .contains("\"id\":" + notificacao.getId())
                .contains("\"mensagem\":\"Seu pedido foi aprovado.\"")
                .contains("\"recurso\":\"pedido\"")
                .contains("\"recursoId\":10042");
    }

    @Test
    void dec13_ultimoEventoReenviaAsNotificacoesPersistidasPosteriores() throws Exception {
        Notificacao primeira = notificar.executar(Notificar.Comando.semReferencia(cliente.getId(),
                TipoNotificacao.CARRINHO_EXPIRANDO, "Seu carrinho expira em 5 minutos."));
        Notificacao segunda = notificar.executar(Notificar.Comando.semReferencia(cliente.getId(),
                TipoNotificacao.CARRINHO_EXPIRADO, "Os itens do seu carrinho foram removidos."));

        MvcResult resultado = abrirStream(primeira.getId());

        String corpo = corpo(resultado);
        assertThat(corpo).contains("id:" + segunda.getId());
        assertThat(corpo).doesNotContain("id:" + primeira.getId());
    }

    @Test
    void dec13_logoutEncerraOCanalDoUsuario() throws Exception {
        MvcResult resultado = abrirStream(null);
        assertThat(registro.contar(cliente.getId())).isEqualTo(1);

        mvc.perform(delete("/api/sessao").cookie(cookieDe(cliente)).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(registro.contar(cliente.getId())).isZero();
        Notificacao depoisDoLogout = notificar.executar(Notificar.Comando.semReferencia(cliente.getId(),
                TipoNotificacao.PEDIDO_ENTREGUE, "Pedido entregue."));
        assertThat(corpo(resultado)).doesNotContain("id:" + depoisDoLogout.getId());
    }

    @Test
    void dec13_rotaDeAdminAbreOProprioStream() throws Exception {
        Usuario admin = usuarios.buscarPorId(1L).orElseThrow();

        MvcResult resultado = mvc.perform(get("/api/admin/eventos").cookie(cookieDe(admin)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        Notificacao notificacao = notificar.executar(Notificar.Comando.semReferencia(admin.getId(),
                TipoNotificacao.TROCA_SOLICITADA, "Nova troca para autorizar."));

        String corpo = corpo(resultado);
        assertThat(corpo)
                .contains("id:" + notificacao.getId())
                .contains("event:notificacao")
                .contains("\"tipo\":\"TROCA_SOLICITADA\"");

        encerrarCanais.executar(admin.getId());
    }

    @Test
    void dec08_streamSemSessaoResponde401() throws Exception {
        mvc.perform(get("/api/me/eventos"))
                .andExpect(status().isUnauthorized());
    }

    private MvcResult abrirStream(Long ultimoEvento) throws Exception {
        var requisicao = get("/api/me/eventos").cookie(cookieDe(cliente));
        if (ultimoEvento != null) {
            requisicao.header("Last-Event-ID", String.valueOf(ultimoEvento));
        }
        return mvc.perform(requisicao)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();
    }

    private static String corpo(MvcResult resultado) throws Exception {
        return resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private Cookie cookieDe(Usuario usuario) {
        return new Cookie(EmissorDeToken.COOKIE, emissor.emitir(usuario).valor());
    }
}
