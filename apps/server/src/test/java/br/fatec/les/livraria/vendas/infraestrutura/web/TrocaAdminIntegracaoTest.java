package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocaRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;
import br.fatec.les.livraria.vendas.dominio.troca.ItemTroca;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

/**
 * Autorização, recusa e consulta de trocas pelo admin (RF-VEN-012, RF-VEN-013, RN-VEN-016, DEC-12, DEC-13). Os
 * pedidos da Ana gravados aqui têm dois exemplares de Dom Casmurro e são desfeitos no fim.
 */
@SpringBootTest
@Transactional
class TrocaAdminIntegracaoTest {

    private static final ClienteNoPedido ANA = new ClienteNoPedido(1L, "Ana Souza", "52998224725",
            "ana.souza@livraria.test", "(11) 98888-7001");

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private PedidoRepositorio pedidos;

    @Autowired
    private TrocaRepositorio trocas;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mvc;
    private Pedido pedido;
    private long item;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        LocalDateTime entregueEm = LocalDateTime.now(RelogioDoSistema.FUSO).minusDays(1);
        Pedido novo = Pedidos.emProcessamento(null, ANA, entregueEm.minusDays(3));
        novo.aprovar();
        novo.despachar();
        novo.confirmarEntrega(entregueEm);
        pedido = pedidos.salvar(novo);
        item = pedido.getItens().getFirst().id();
    }

    @Test
    void rfVen012_autorizaATrocaEAvisaOCliente() throws Exception {
        long troca = solicitar(1);

        acao(troca, "autorizar", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedido.getId()))
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.trocas[0].status").value("TROCA_AUTORIZADA"))
                .andExpect(jsonPath("$.troca.situacao").value("TROCA_AUTORIZADA"));
        entityManager.flush();
        assertThat(notificacoes("TROCA_AUTORIZADA")).isEqualTo(1);

        acao(troca, "autorizar", null).andExpect(status().isConflict());
        acao(troca, "recusar", "{\"motivo\":\"Tarde demais\"}").andExpect(status().isConflict());
    }

    @Test
    void dec12_recusaExigeMotivoAvisaOClienteEImpedeNovaTrocaDoItem() throws Exception {
        long troca = solicitar(2);

        acao(troca, "recusar", "{}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RF-VEN-012"));
        acao(troca, "recusar", "{\"motivo\":\"" + "a".repeat(301) + "\"}")
                .andExpect(status().isUnprocessableContent());
        String motivo = "b".repeat(300);
        acao(troca, "recusar", "{\"motivo\":\"" + motivo + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocas[0].status").value("TROCA_RECUSADA"))
                .andExpect(jsonPath("$.trocas[0].motivoRecusa").value(motivo))
                .andExpect(jsonPath("$.troca.situacao").value("TROCA_RECUSADA"))
                .andExpect(jsonPath("$.itens[0].disponivelParaTroca").value(0));
        entityManager.flush();
        assertThat(jdbc.queryForObject("select mensagem from notificacao where usuario_id = 2 and tipo = "
                + "'TROCA_RECUSADA' and recurso = 'pedido' and recurso_id = ?", String.class, pedido.getId()))
                .endsWith("Motivo: " + motivo);

        mvc.perform(post("/api/me/pedidos/" + pedido.getId() + "/trocas").with(csrf()).cookie(cookieDoUsuario(2L))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itens\":[{\"itemPedidoId\":%d,\"quantidade\":1}]}".formatted(item)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void rfVen013_consultaDeTrocasPeloStatusDaTrocaEFiltroDePedidosPelaMaisRecente() throws Exception {
        long pendente = solicitar(1);
        long autorizada = solicitar(1);
        acao(autorizada, "autorizar", null).andExpect(status().isOk());
        entityManager.flush();

        mvc.perform(comoAdmin(get("/api/admin/trocas").param("status", "EM_TROCA").param("tamanho", "100")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].id", hasItem((int) pendente)))
                .andExpect(jsonPath("$.itens[*].id", not(hasItem((int) autorizada))))
                .andExpect(jsonPath("$.itens[?(@.id == %d)].pedidoId".formatted(pendente)).value(
                        hasItem(pedido.getId().intValue())))
                .andExpect(jsonPath("$.itens[?(@.id == %d)].clienteNome".formatted(pendente)).value(
                        hasItem("Ana Souza")))
                .andExpect(jsonPath("$.itens[?(@.id == %d)].unidades".formatted(pendente)).value(hasItem(1)));

        mvc.perform(comoAdmin(get("/api/admin/pedidos").param("situacao", "EM_TROCA").param("tamanho", "100")))
                .andExpect(jsonPath("$.itens[*].id", not(hasItem(pedido.getId().intValue()))));
        mvc.perform(comoAdmin(get("/api/admin/pedidos").param("situacao", "TROCA_AUTORIZADA", "TROCADO")))
                .andExpect(jsonPath("$.itens[*].id", hasItem(pedido.getId().intValue())));
    }

    @Test
    void dec07_trocaInexistenteResponde404ESoOAdminAcessa() throws Exception {
        acao(999_999L, "autorizar", null).andExpect(status().isNotFound());

        long troca = solicitar(1);
        mvc.perform(post("/api/admin/trocas/" + troca + "/autorizar").with(csrf()).cookie(cookieDoUsuario(2L)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/trocas").cookie(cookieDoUsuario(2L))).andExpect(status().isForbidden());
    }

    private long solicitar(int quantidade) {
        return trocas.salvar(Troca.solicitar(pedido.getId(), List.of(new ItemTroca(null, item, quantidade, null)),
                LocalDateTime.now(RelogioDoSistema.FUSO))).getId();
    }

    private ResultActions acao(long troca, String acao, String corpo) throws Exception {
        MockHttpServletRequestBuilder requisicao = post("/api/admin/trocas/" + troca + "/" + acao).with(csrf()).cookie(cookieDoUsuario(1L));
        if (corpo != null) {
            requisicao = requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        return mvc.perform(requisicao);
    }

    private int notificacoes(String tipo) {
        return jdbc.queryForObject("select count(*) from notificacao where usuario_id = 2 and tipo = ? "
                + "and recurso = 'pedido' and recurso_id = ?", Integer.class, tipo, pedido.getId());
    }

    private MockHttpServletRequestBuilder comoAdmin(MockHttpServletRequestBuilder requisicao) {
        return requisicao.cookie(cookieDoUsuario(1L));
    }

    private Cookie cookieDoUsuario(Long id) {
        String token = emissor.emitir(usuarios.buscarPorId(id).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
