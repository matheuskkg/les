package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

/**
 * Solicitação de troca pelo cliente logado (RF-VEN-011, RN-VEN-011, RN-VEN-013, DEC-12, DEC-13). Os pedidos gravados
 * aqui têm dois exemplares de Dom Casmurro e são desfeitos no fim.
 */
@SpringBootTest
@Transactional
class SolicitacaoDeTrocaIntegracaoTest {

    private static final ClienteNoPedido ANA = new ClienteNoPedido(1L, "Ana Souza", "52998224725",
            "ana.souza@livraria.test", "(11) 98888-7001");
    private static final ClienteNoPedido BRUNO = new ClienteNoPedido(2L, "Bruno Lima", "11144477735",
            "bruno.lima@livraria.test", "(21) 98888-7002");

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private PedidoRepositorio pedidos;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfVen011_criaTrocaEmTrocaMantemOPedidoEntregueEAvisaOAdmin() throws Exception {
        Pedido pedido = entregue(ANA, 1);
        long item = pedido.getItens().getFirst().id();

        solicitar(pedido.getId(), item, 1)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/me/pedidos/" + pedido.getId()))
                .andExpect(jsonPath("$.status").value("EM_TROCA"))
                .andExpect(jsonPath("$.pedidoId").value(pedido.getId()))
                .andExpect(jsonPath("$.itens[0].itemPedidoId").value(item))
                .andExpect(jsonPath("$.itens[0].quantidade").value(1));
        entityManager.flush();

        mvc.perform(get("/api/me/pedidos/" + pedido.getId()).cookie(cookieDoUsuario(2L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.troca.situacao").value("EM_TROCA"))
                .andExpect(jsonPath("$.troca.abrangencia").value("PARCIAL"))
                .andExpect(jsonPath("$.podeSolicitarTroca").value(true))
                .andExpect(jsonPath("$.itens[0].disponivelParaTroca").value(1))
                .andExpect(jsonPath("$.itens[0].statusDaTroca").value("EM_TROCA"))
                .andExpect(jsonPath("$.trocas.length()").value(1));
        assertThat(jdbc.queryForObject("select count(*) from notificacao where usuario_id = 1 and tipo = "
                + "'TROCA_SOLICITADA' and recurso = 'pedido' and recurso_id = ?", Integer.class, pedido.getId()))
                .isEqualTo(1);
    }

    @Test
    void rfVen011_aQuantidadeJaPedidaNaoPodeSerPedidaDeNovo() throws Exception {
        Pedido pedido = entregue(ANA, 1);
        long item = pedido.getItens().getFirst().id();

        solicitar(pedido.getId(), item, 2).andExpect(status().isCreated());
        entityManager.flush();
        mvc.perform(get("/api/me/pedidos/" + pedido.getId()).cookie(cookieDoUsuario(2L)))
                .andExpect(jsonPath("$.podeSolicitarTroca").value(false));
        solicitar(pedido.getId(), item, 1)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RF-VEN-011"));
    }

    @Test
    void rfVen011_quantidadeAcimaDaCompradaResponde422() throws Exception {
        Pedido pedido = entregue(ANA, 1);

        solicitar(pedido.getId(), pedido.getItens().getFirst().id(), 3)
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void rnVen013_pedidoNaoEntregueOuForaDoPrazoResponde422() throws Exception {
        Pedido aprovado = Pedidos.emProcessamento(null, ANA, LocalDateTime.of(2001, 3, 10, 10, 0));
        aprovado.aprovar();
        aprovado = pedidos.salvar(aprovado);
        Pedido antigo = entregue(ANA, 30);
        mvc.perform(get("/api/me/pedidos/" + antigo.getId()).cookie(cookieDoUsuario(2L)))
                .andExpect(jsonPath("$.podeSolicitarTroca").value(false));

        solicitar(aprovado.getId(), aprovado.getItens().getFirst().id(), 1)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-013"));
        solicitar(antigo.getId(), antigo.getItens().getFirst().id(), 1)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-013"));
    }

    @Test
    void rfVen011_pedidoDeOutroClienteResponde404() throws Exception {
        Pedido doBruno = entregue(BRUNO, 1);

        solicitar(doBruno.getId(), doBruno.getItens().getFirst().id(), 1).andExpect(status().isNotFound());
    }

    private Pedido entregue(ClienteNoPedido cliente, int diasDesdeAEntrega) {
        LocalDateTime entregueEm = LocalDateTime.now(RelogioDoSistema.FUSO).minusDays(diasDesdeAEntrega);
        Pedido pedido = Pedidos.emProcessamento(null, cliente, entregueEm.minusDays(3));
        pedido.aprovar();
        pedido.despachar();
        pedido.confirmarEntrega(entregueEm);
        return pedidos.salvar(pedido);
    }

    private ResultActions solicitar(long pedidoId, long itemPedidoId, int quantidade) throws Exception {
        return mvc.perform(post("/api/me/pedidos/" + pedidoId + "/trocas").with(csrf()).cookie(cookieDoUsuario(2L))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itens\":[{\"itemPedidoId\":%d,\"quantidade\":%d}]}".formatted(itemPedidoId, quantidade)));
    }

    private Cookie cookieDoUsuario(Long id) {
        String token = emissor.emitir(usuarios.buscarPorId(id).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
