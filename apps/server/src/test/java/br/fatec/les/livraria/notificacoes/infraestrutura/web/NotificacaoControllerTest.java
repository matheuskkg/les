package br.fatec.les.livraria.notificacoes.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import jakarta.servlet.http.Cookie;

/**
 * Rotas de notificação do cliente e do administrador (DEC-13, DEC-07, DEC-08, RN-VEN-016). O id do usuário vem da
 * sessão: uma notificação de outro cliente responde 404, e as rotas de admin exigem o papel {@code ADMIN}.
 */
@SpringBootTest
@Transactional
class NotificacaoControllerTest {

    private static final String SENHA = "Senha@123";
    private static final LocalDateTime CRIADA_EM = LocalDateTime.of(2026, 10, 24, 8, 0);

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private CodificadorDeSenha senhas;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private NotificacaoRepositorio notificacoes;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void dec13_clienteListaSomenteAsPropriasNotificacoesNoEnvelopePaginado() throws Exception {
        Usuario cliente = criarCliente("notificacoes.lista@livraria.test");
        criar(cliente.getId(), TipoNotificacao.PEDIDO_APROVADO);
        criar(cliente.getId(), TipoNotificacao.PEDIDO_ENTREGUE);
        criar(999_999L, TipoNotificacao.PEDIDO_APROVADO);

        mvc.perform(get("/api/me/notificacoes").cookie(cookieDe(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalItens").value(2))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.itens[*].tipo", contains(
                        TipoNotificacao.PEDIDO_ENTREGUE.name(), TipoNotificacao.PEDIDO_APROVADO.name())));
    }

    @Test
    void dec13_listaNaoLidasPrimeiroEMaisRecentesAntes() throws Exception {
        Usuario cliente = criarCliente("notificacoes.ordem@livraria.test");
        Notificacao lida = criar(cliente.getId(), TipoNotificacao.PEDIDO_APROVADO, CRIADA_EM.plusHours(3));
        lida.marcarComoLida(CRIADA_EM.plusHours(4));
        notificacoes.salvar(lida);
        Notificacao antigaNaoLida = criar(cliente.getId(), TipoNotificacao.PEDIDO_ENTREGUE, CRIADA_EM);
        Notificacao recenteNaoLida = criar(cliente.getId(), TipoNotificacao.TROCA_AUTORIZADA, CRIADA_EM.plusHours(1));

        mvc.perform(get("/api/me/notificacoes").cookie(cookieDe(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(recenteNaoLida.getId()))
                .andExpect(jsonPath("$.itens[0].lidaEm").doesNotExist())
                .andExpect(jsonPath("$.itens[1].id").value(antigaNaoLida.getId()))
                .andExpect(jsonPath("$.itens[2].id").value(lida.getId()))
                .andExpect(jsonPath("$.itens[2].lidaEm").exists());
    }

    @Test
    void dec13_contaAsNaoLidasDoCliente() throws Exception {
        Usuario cliente = criarCliente("notificacoes.contador@livraria.test");
        criar(cliente.getId(), TipoNotificacao.CARRINHO_EXPIRANDO);
        Notificacao lida = criar(cliente.getId(), TipoNotificacao.CHECKOUT_EXPIRANDO);
        lida.marcarComoLida(CRIADA_EM.plusHours(1));
        notificacoes.salvar(lida);

        mvc.perform(get("/api/me/notificacoes/nao-lidas").cookie(cookieDe(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(1));
    }

    @Test
    void dec13_marcaUmaNotificacaoDoClienteComoLida() throws Exception {
        Usuario cliente = criarCliente("notificacoes.ler.uma@livraria.test");
        Notificacao notificacao = criar(cliente.getId(), TipoNotificacao.PEDIDO_DESPACHADO);

        mvc.perform(post("/api/me/notificacoes/{id}/ler", notificacao.getId())
                .cookie(cookieDe(cliente)).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/me/notificacoes/nao-lidas").cookie(cookieDe(cliente)))
                .andExpect(jsonPath("$.quantidade").value(0));
        mvc.perform(get("/api/me/notificacoes").cookie(cookieDe(cliente)))
                .andExpect(jsonPath("$.itens[0].lidaEm").exists());
    }

    @Test
    void dec13_marcaTodasAsNotificacoesDoClienteComoLidas() throws Exception {
        Usuario cliente = criarCliente("notificacoes.ler.todas@livraria.test");
        criar(cliente.getId(), TipoNotificacao.CARRINHO_EXPIRANDO);
        criar(cliente.getId(), TipoNotificacao.CHECKOUT_EXPIRANDO);

        mvc.perform(post("/api/me/notificacoes/ler-todas").cookie(cookieDe(cliente)).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/me/notificacoes/nao-lidas").cookie(cookieDe(cliente)))
                .andExpect(jsonPath("$.quantidade").value(0));
    }

    @Test
    void dec14_idGeradoFicaAcimaDaFaixaReservadaAoSeed() throws Exception {
        Usuario cliente = criarCliente("notificacoes.id@livraria.test");

        Notificacao notificacao = criar(cliente.getId(), TipoNotificacao.CARRINHO_EXPIRANDO);

        assertThat(notificacao.getId()).isGreaterThanOrEqualTo(10000L);
    }

    @Test
    void dec07_marcarNotificacaoDeOutroClienteResponde404() throws Exception {
        Usuario cliente = criarCliente("notificacoes.dono@livraria.test");
        Notificacao deOutro = criar(999_999L, TipoNotificacao.PEDIDO_APROVADO);

        mvc.perform(post("/api/me/notificacoes/{id}/ler", deOutro.getId())
                .cookie(cookieDe(cliente)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void dec07_marcarNotificacaoInexistenteResponde404() throws Exception {
        Usuario cliente = criarCliente("notificacoes.inexistente@livraria.test");

        mvc.perform(post("/api/me/notificacoes/{id}/ler", 999_999L)
                .cookie(cookieDe(cliente)).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Notificação não encontrada."));
    }

    @Test
    void dec08_rotasDoClienteSemSessaoRespondem401() throws Exception {
        mvc.perform(get("/api/me/notificacoes"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me/notificacoes/nao-lidas"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/me/notificacoes/ler-todas").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dec08_rotasDeAdminExigemPapelAdmin() throws Exception {
        Usuario cliente = criarCliente("notificacoes.admin.403@livraria.test");

        mvc.perform(get("/api/admin/notificacoes").cookie(cookieDe(cliente)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/notificacoes/ler-todas").cookie(cookieDe(cliente)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void dec13_adminListaEContaAsPropriasNotificacoes() throws Exception {
        Usuario admin = usuarios.buscarPorId(1L).orElseThrow();
        Notificacao notificacao = criar(admin.getId(), TipoNotificacao.TROCA_SOLICITADA);

        mvc.perform(get("/api/admin/notificacoes").cookie(cookieDe(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].id", org.hamcrest.Matchers.hasItem(notificacao.getId().intValue())));
        mvc.perform(get("/api/admin/notificacoes/nao-lidas").cookie(cookieDe(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/admin/notificacoes/{id}/ler", notificacao.getId())
                .cookie(cookieDe(admin)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void dec07_ordemForaDosCamposPermitidosResponde400() throws Exception {
        Usuario cliente = criarCliente("notificacoes.ordem.invalida@livraria.test");

        mvc.perform(get("/api/me/notificacoes").param("ordem", "mensagem,asc").cookie(cookieDe(cliente)))
                .andExpect(status().isBadRequest());
    }

    private Usuario criarCliente(String email) {
        return usuarios.salvar(new Usuario(null, email, null, senhas.codificar(SENHA), Papel.CLIENTE, null));
    }

    private Notificacao criar(Long usuarioId, TipoNotificacao tipo) {
        return criar(usuarioId, tipo, CRIADA_EM);
    }

    private Notificacao criar(Long usuarioId, TipoNotificacao tipo, LocalDateTime criadaEm) {
        return notificacoes.salvar(Notificacao.nova(usuarioId, tipo, "Notificação de teste.", "pedido", 10042L,
                criadaEm));
    }

    private Cookie cookieDe(Usuario usuario) {
        return new Cookie(EmissorDeToken.COOKIE, emissor.emitir(usuario).valor());
    }
}
