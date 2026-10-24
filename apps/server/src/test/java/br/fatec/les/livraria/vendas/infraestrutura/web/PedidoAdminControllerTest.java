package br.fatec.les.livraria.vendas.infraestrutura.web;

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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

/**
 * Consulta de pedidos do administrador (RF-VEN-009, RF-VEN-010, DEC-07, DEC-12). Cada teste grava três pedidos em
 * março de 2001 e desfaz tudo no fim; toda consulta filtra esse ano, para não depender dos pedidos que outros testes
 * deixam no banco. Cada pedido tem dois Dom Casmurro, frete de R$ 12,00 e R$ 10,00 de desconto: R$ 97,76. O
 * despacho e a entrega seguem o diagrama do DEC-12 (RN-VEN-009, RN-VEN-010, DEC-13), com o relógio fixo em
 * 2026-10-24T12:00 no fuso da loja.
 */
@SpringBootTest
@Transactional
class PedidoAdminControllerTest {

    private static final String ROTA = "/api/admin/pedidos";
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final ClienteNoPedido ANA = new ClienteNoPedido(1L, "Ana Souza", "52998224725",
            "ana.souza@livraria.test", "(11) 98888-7001");
    private static final ClienteNoPedido BRUNO = new ClienteNoPedido(2L, "Bruno Lima", "11144477735",
            "bruno.lima@livraria.test", "(21) 98888-7002");

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
    private JdbcClient jdbc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private PedidoRepositorio pedidos;

    private MockMvc mvc;
    private long aprovadoDaAna;
    private long reprovadoDaAna;
    private long aprovadoDoBruno;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        aprovadoDaAna = gravar(ANA, LocalDateTime.of(2001, 3, 10, 10, 0), true);
        aprovadoDoBruno = gravar(BRUNO, LocalDateTime.of(2001, 3, 11, 9, 0), true);
        reprovadoDaAna = gravar(ANA, LocalDateTime.of(2001, 3, 12, 23, 30), false);
    }

    @Test
    void rfVen009_listaOsPedidosDoPeriodoDoMaisRecenteAoMaisAntigo() throws Exception {
        mvc.perform(comoAdmin(em2001(get(ROTA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(3))
                .andExpect(jsonPath("$.itens[*].id", contains(
                        (int) reprovadoDaAna, (int) aprovadoDoBruno, (int) aprovadoDaAna)))
                .andExpect(jsonPath("$.itens[0].status").value("REPROVADA"))
                .andExpect(jsonPath("$.itens[0].clienteNome").value("Ana Souza"))
                .andExpect(jsonPath("$.itens[0].clienteCpf").value("52998224725"))
                .andExpect(jsonPath("$.itens[0].total").value("97.76"))
                .andExpect(jsonPath("$.itens[0].realizadoEm").value("2001-03-13T02:30:00Z"));
    }

    @Test
    void rfVen009_statusRepetidosCombinamComOu() throws Exception {
        mvc.perform(comoAdmin(em2001(get(ROTA)).param("status", "APROVADA").param("status", "EM_TRANSPORTE")))
                .andExpect(jsonPath("$.itens[*].id", contains((int) aprovadoDoBruno, (int) aprovadoDaAna)));
    }

    @Test
    void rfVen009_filtraPorNomeECpfDoClienteComE() throws Exception {
        mvc.perform(comoAdmin(em2001(get(ROTA)).param("cliente", "ANA souza")))
                .andExpect(jsonPath("$.itens[*].id", contains((int) reprovadoDaAna, (int) aprovadoDaAna)));
        mvc.perform(comoAdmin(em2001(get(ROTA)).param("cpf", "111.444.777-35")))
                .andExpect(jsonPath("$.itens[*].id", contains((int) aprovadoDoBruno)));
        mvc.perform(comoAdmin(em2001(get(ROTA)).param("cliente", "Ana").param("cpf", "11144477735")))
                .andExpect(jsonPath("$.totalItens").value(0));
    }

    @Test
    void rfCli005_filtraPeloCodigoDoClienteParaOHistoricoDoCadastro() throws Exception {
        mvc.perform(comoAdmin(em2001(get(ROTA)).param("clienteId", "2")))
                .andExpect(jsonPath("$.itens[*].id", contains((int) aprovadoDoBruno)));
        mvc.perform(comoAdmin(em2001(get(ROTA)).param("clienteId", "1")))
                .andExpect(jsonPath("$.itens[*].id", contains((int) reprovadoDaAna, (int) aprovadoDaAna)));
    }

    @Test
    void rfVen009_clienteSemCorrespondenciaNaoListaNenhumPedido() throws Exception {
        mvc.perform(comoAdmin(em2001(get(ROTA)).param("cliente", "Ninguém com esse nome")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(0));
    }

    @Test
    void rfVen009_filtraPeloNumeroDoPedido() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("numero", String.valueOf(aprovadoDoBruno))))
                .andExpect(jsonPath("$.itens[*].id", contains((int) aprovadoDoBruno)));
    }

    @Test
    void rfVen009_ultimoDiaDoPeriodoEntraInteiro() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("realizadoEmDe", "2001-03-11").param("realizadoEmAte", "2001-03-12")))
                .andExpect(jsonPath("$.itens[*].id", contains((int) reprovadoDaAna, (int) aprovadoDoBruno)));
    }

    @Test
    void dec07_ordenaPorTotalEPaginaComDesempatePeloNumero() throws Exception {
        mvc.perform(comoAdmin(em2001(get(ROTA)).param("ordem", "total,asc").param("tamanho", "2")))
                .andExpect(jsonPath("$.totalItens").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.itens[*].id", contains((int) aprovadoDaAna, (int) aprovadoDoBruno)));
    }

    @Test
    void dec07_parametrosInvalidosRespondem400() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("realizadoEmDe", "2001-03-12").param("realizadoEmAte", "2001-03-11")))
                .andExpect(status().isBadRequest());
        mvc.perform(comoAdmin(get(ROTA).param("cpf", "abc"))).andExpect(status().isBadRequest());
        mvc.perform(comoAdmin(get(ROTA).param("status", "CANCELADO"))).andExpect(status().isBadRequest());
        mvc.perform(comoAdmin(get(ROTA).param("numero", "abc"))).andExpect(status().isBadRequest());
        mvc.perform(comoAdmin(get(ROTA).param("ordem", "cliente,asc"))).andExpect(status().isBadRequest());
    }

    @Test
    void rfVen009_detalheTrazAsCopiasDoPedido() throws Exception {
        mvc.perform(comoAdmin(get(ROTA + "/" + aprovadoDaAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aprovadoDaAna))
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.entregueEm").doesNotExist())
                .andExpect(jsonPath("$.cliente.cpf").value("52998224725"))
                .andExpect(jsonPath("$.enderecoEntrega.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.enderecoCobranca.cep").value("01415000"))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].livro.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.itens[0].livro.categorias[*].nome", contains("Romance", "Clássicos")))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value("47.88"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.itens[0].subtotal").value("95.76"))
                .andExpect(jsonPath("$.cartoes[0].cartao.ultimosDigitos").value("1111"))
                .andExpect(jsonPath("$.cartoes[0].valor").value("57.76"))
                .andExpect(jsonPath("$.cupons[*].cupom.codigo", contains("BEMVINDO10", "TROCA-ANA-40")))
                .andExpect(jsonPath("$.subtotal").value("95.76"))
                .andExpect(jsonPath("$.frete").value("12.00"))
                .andExpect(jsonPath("$.descontoPromocional").value("10.00"))
                .andExpect(jsonPath("$.total").value("97.76"));
    }

    @Test
    void rfVen009_pedidoInexistenteResponde404() throws Exception {
        mvc.perform(comoAdmin(get(ROTA + "/999999"))).andExpect(status().isNotFound());
    }

    @Test
    void dec08_consultaExigePapelAdmin() throws Exception {
        mvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
        mvc.perform(get(ROTA).cookie(cookieDoUsuario(2L))).andExpect(status().isForbidden());
        mvc.perform(get(ROTA + "/" + aprovadoDaAna).cookie(cookieDoUsuario(2L))).andExpect(status().isForbidden());
    }

    @Test
    void rnVen009_rnVen010_despachaEConfirmaAEntregaComNotificacoesAoCliente() throws Exception {
        mvc.perform(comoAdmin(post(ROTA + "/" + aprovadoDaAna + "/despachar").with(csrf())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_TRANSPORTE"))
                .andExpect(jsonPath("$.entregueEm").doesNotExist());

        mvc.perform(comoAdmin(post(ROTA + "/" + aprovadoDaAna + "/confirmar-entrega").with(csrf())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.entregueEm").value("2026-10-24T15:00:00Z"));

        mvc.perform(comoAdmin(get(ROTA + "/" + aprovadoDaAna)))
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.entregueEm").value("2026-10-24T15:00:00Z"));
        entityManager.flush();
        assertThat(jdbc.sql("select tipo from notificacao where usuario_id = 2 and recurso_id = ?")
                .param(aprovadoDaAna).query(String.class).list())
                .containsExactlyInAnyOrder("PEDIDO_DESPACHADO", "PEDIDO_ENTREGUE");
    }

    @Test
    void dec12_transicoesForaDoDiagramaRespondem409() throws Exception {
        mvc.perform(comoAdmin(post(ROTA + "/" + reprovadoDaAna + "/despachar").with(csrf()))).andExpect(status().isConflict());
        mvc.perform(comoAdmin(post(ROTA + "/" + aprovadoDaAna + "/confirmar-entrega").with(csrf())))
                .andExpect(status().isConflict());

        mvc.perform(comoAdmin(post(ROTA + "/" + aprovadoDaAna + "/despachar").with(csrf()))).andExpect(status().isOk());
        mvc.perform(comoAdmin(post(ROTA + "/" + aprovadoDaAna + "/despachar").with(csrf()))).andExpect(status().isConflict());
        mvc.perform(comoAdmin(post(ROTA + "/" + aprovadoDaAna + "/confirmar-entrega").with(csrf()))).andExpect(status().isOk());
        mvc.perform(comoAdmin(post(ROTA + "/" + aprovadoDaAna + "/confirmar-entrega").with(csrf())))
                .andExpect(status().isConflict());
    }

    @Test
    void rfVen009_acaoEmPedidoInexistenteResponde404EExigeAdmin() throws Exception {
        mvc.perform(comoAdmin(post(ROTA + "/999999/despachar").with(csrf()))).andExpect(status().isNotFound());
        mvc.perform(comoAdmin(post(ROTA + "/999999/confirmar-entrega").with(csrf()))).andExpect(status().isNotFound());
        mvc.perform(post(ROTA + "/" + aprovadoDaAna + "/despachar").with(csrf()).cookie(cookieDoUsuario(2L)))
                .andExpect(status().isForbidden());
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

    private static MockHttpServletRequestBuilder em2001(MockHttpServletRequestBuilder requisicao) {
        return requisicao.param("realizadoEmDe", "2001-01-01").param("realizadoEmAte", "2001-12-31");
    }

    private MockHttpServletRequestBuilder comoAdmin(MockHttpServletRequestBuilder requisicao) {
        return requisicao.cookie(cookieDoUsuario(1L));
    }

    private Cookie cookieDoUsuario(Long id) {
        String token = emissor.emitir(usuarios.buscarPorId(id).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
