package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

/**
 * "Meus pedidos" do cliente logado (RF-CLI-005, DEC-07, DEC-12). Grava pedidos da Ana (usuário 2, cliente 1) e do Bruno
 * (usuário 3, cliente 2) e desfaz tudo no fim; como outros testes podem deixar pedidos no banco, as asserções olham os
 * números gravados aqui.
 */
@SpringBootTest
@Transactional
class MeusPedidosControllerTest {

    private static final String ROTA = "/api/me/pedidos";
    private static final ClienteNoPedido ANA =
            new ClienteNoPedido(1L, "Ana Souza", "52998224725", "ana.souza@livraria.test", "(11) 98888-7001");
    private static final ClienteNoPedido BRUNO =
            new ClienteNoPedido(2L, "Bruno Lima", "11144477735", "bruno.lima@livraria.test", "(21) 98888-7002");

    @Autowired private WebApplicationContext contexto;

    @Autowired private UsuarioRepositorio usuarios;

    @Autowired private EmissorDeToken emissor;

    @Autowired private PedidoRepositorio pedidos;

    private MockMvc mvc;
    private long aprovadoDaAna;
    private long reprovadoDaAna;
    private long doBruno;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        aprovadoDaAna = gravar(ANA, LocalDateTime.of(2001, 3, 10, 10, 0), true);
        reprovadoDaAna = gravar(ANA, LocalDateTime.of(2001, 3, 12, 10, 0), false);
        doBruno = gravar(BRUNO, LocalDateTime.of(2001, 3, 11, 10, 0), true);
    }

    @Test
    void rfCli005_listaTodasAsTransacoesDoClienteLogadoInclusiveReprovadas() throws Exception {
        mvc.perform(comoAna(get(ROTA).param("tamanho", "100")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].id", hasItems((int) aprovadoDaAna, (int) reprovadoDaAna)))
                .andExpect(jsonPath("$.itens[*].id", not(hasItem((int) doBruno))))
                .andExpect(jsonPath("$.itens[*].clienteId", not(hasItem(2))));
    }

    @Test
    void rfCli005_detalheDoProprioPedidoTrazAsCopias() throws Exception {
        mvc.perform(comoAna(get(ROTA + "/" + aprovadoDaAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.itens[0].livro.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.total").value("97.76"));
    }

    @Test
    void rfCli005_pedidoDeOutroClienteOuInexistenteResponde404() throws Exception {
        mvc.perform(comoAna(get(ROTA + "/" + doBruno))).andExpect(status().isNotFound());
        mvc.perform(comoAna(get(ROTA + "/999999"))).andExpect(status().isNotFound());
    }

    @Test
    void dec07_ordemForaDaListaResponde400() throws Exception {
        mvc.perform(comoAna(get(ROTA).param("ordem", "cliente,asc"))).andExpect(status().isBadRequest());
    }

    @Test
    void dec08_exigeClienteLogado() throws Exception {
        mvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
        mvc.perform(get(ROTA).cookie(cookieDoUsuario(1L))).andExpect(status().isForbidden());
    }

    private long gravar(ClienteNoPedido cliente, LocalDateTime realizadoEm, boolean aprovar) {
        Pedido pedido = Pedidos.emProcessamento(null, cliente, realizadoEm);
        if (aprovar) {
            pedido.aprovar();
        } else {
            pedido.reprovar();
        }
        return pedidos.salvar(pedido).getId();
    }

    private MockHttpServletRequestBuilder comoAna(MockHttpServletRequestBuilder requisicao) {
        return requisicao.cookie(cookieDoUsuario(2L));
    }

    private Cookie cookieDoUsuario(Long id) {
        String token = emissor.emitir(usuarios.buscarPorId(id).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
