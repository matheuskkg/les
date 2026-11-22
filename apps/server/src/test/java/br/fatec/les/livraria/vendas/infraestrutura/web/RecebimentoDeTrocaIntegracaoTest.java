package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Recebimento dos itens da troca pelo admin, com reentrada no estoque e cupom de troca (RF-VEN-014, RF-VEN-015,
 * RF-EST-004, RN-VEN-012, DEC-11, DEC-12, DEC-13). Os pedidos da Ana gravados aqui têm dois exemplares de Dom Casmurro
 * (livro 1, R$ 47,88) e são desfeitos no fim.
 */
@SpringBootTest
@Transactional
class RecebimentoDeTrocaIntegracaoTest {

    private static final ClienteNoPedido ANA =
            new ClienteNoPedido(1L, "Ana Souza", "52998224725", "ana.souza@livraria.test", "(11) 98888-7001");

    @Autowired private WebApplicationContext contexto;

    @Autowired private UsuarioRepositorio usuarios;

    @Autowired private EmissorDeToken emissor;

    @Autowired private PedidoRepositorio pedidos;

    @Autowired private TrocaRepositorio trocas;

    @Autowired private JdbcTemplate jdbc;

    @Autowired private EntityManager entityManager;

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
    void rfVen015_recebimentoReentraNoEstoqueGeraOCupomEConcluiATroca() throws Exception {
        long troca = autorizada(2);
        int antes = estoqueDoDomCasmurro();

        receber(troca, "{\"itens\":[{\"itemPedidoId\":%d,\"voltaEstoque\":true}]}".formatted(item))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.trocas[0].status").value("TROCADO"))
                .andExpect(jsonPath("$.trocas[0].valorCupom").value("95.76"))
                .andExpect(jsonPath("$.trocas[0].itens[0].voltaEstoque").value(true))
                .andExpect(jsonPath("$.troca.situacao").value("TROCADO"))
                .andExpect(jsonPath("$.troca.abrangencia").value("TOTAL"));
        entityManager.flush();

        assertThat(estoqueDoDomCasmurro()).isEqualTo(antes + 2);
        assertThat(
                        jdbc.queryForObject(
                                "select valor from cupom where codigo = ? and cliente_id = 1 "
                                        + "and situacao = 'DISPONIVEL'",
                                BigDecimal.class,
                                "TROCA-" + troca))
                .isEqualByComparingTo("95.76");
        assertThat(
                        jdbc.queryForObject(
                                "select mensagem from notificacao where usuario_id = 2 and tipo = "
                                        + "'TROCA_CONCLUIDA' and recurso = 'pedido' and recurso_id = ?",
                                String.class,
                                pedido.getId()))
                .contains("TROCA-" + troca)
                .contains("R$ 95,76");

        receber(troca, "{\"itens\":[{\"itemPedidoId\":%d,\"voltaEstoque\":true}]}".formatted(item))
                .andExpect(status().isConflict());
    }

    @Test
    void rfVen014_itemQueNaoVoltaAoEstoqueAindaGeraCupom() throws Exception {
        long troca = autorizada(1);
        int antes = estoqueDoDomCasmurro();

        receber(troca, "{\"itens\":[{\"itemPedidoId\":%d,\"voltaEstoque\":false}]}".formatted(item))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocas[0].valorCupom").value("47.88"));
        entityManager.flush();

        assertThat(estoqueDoDomCasmurro()).isEqualTo(antes);
        assertThat(jdbc.queryForObject("select count(*) from cupom where codigo = ?", Integer.class, "TROCA-" + troca))
                .isEqualTo(1);
    }

    @Test
    void rfVen014_exigeADecisaoDeCadaItemESoTrocaAutorizada() throws Exception {
        long emTroca = solicitar(1);
        receber(emTroca, "{\"itens\":[{\"itemPedidoId\":%d,\"voltaEstoque\":true}]}".formatted(item))
                .andExpect(status().isConflict());

        long troca = autorizada(1);
        receber(troca, "{}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RF-VEN-014"));
        receber(troca, "{\"itens\":[{\"itemPedidoId\":%d}]}".formatted(item))
                .andExpect(status().isUnprocessableContent());
        receber(999_999L, "{}").andExpect(status().isNotFound());
    }

    private long autorizada(int quantidade) throws Exception {
        long troca = solicitar(quantidade);
        mvc.perform(post("/api/admin/trocas/" + troca + "/autorizar").with(csrf()).cookie(cookieDoUsuario(1L)))
                .andExpect(status().isOk());
        return troca;
    }

    private ResultActions receber(long troca, String corpo) throws Exception {
        return mvc.perform(
                post("/api/admin/trocas/" + troca + "/confirmar-recebimento")
                        .with(csrf())
                        .cookie(cookieDoUsuario(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo));
    }

    private int estoqueDoDomCasmurro() {
        entityManager.flush();
        return jdbc.queryForObject("select quantidade from estoque where livro_id = 1", Integer.class);
    }

    private long solicitar(int quantidade) {
        return trocas.salvar(
                        Troca.solicitar(
                                pedido.getId(),
                                List.of(new ItemTroca(null, item, quantidade, null)),
                                LocalDateTime.now(RelogioDoSistema.FUSO)))
                .getId();
    }

    private Cookie cookieDoUsuario(Long id) {
        String token = emissor.emitir(usuarios.buscarPorId(id).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
