package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ExpirarReservas;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import jakarta.servlet.http.Cookie;

/**
 * Sessão de compra sobre o seed (RF-VEN-003, RN-VEN-014, RNF-VEN-001, DEC-10, DEC-12): prazo de 60 min e aviso 5 min
 * antes. Ana é o usuário 2 (cliente 1) e Bruno o 3. Livro 1 tem 20 em estoque e o livro 7, 6.
 */
@SpringBootTest
@Transactional
class CheckoutIntegracaoTest {

    private static final long ANA = 2L;
    private static final long CLIENTE_ANA = 1L;
    private static final long BRUNO = 3L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final AtomicReference<LocalDateTime> AGORA = new AtomicReference<>(INICIO);

    @TestConfiguration
    static class RelogioAjustavelDoTeste {

        @Bean
        @Primary
        Relogio relogioDoTeste() {
            return AGORA::get;
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private ExpirarReservas expirarReservas;

    @Autowired
    private CarrinhoRepositorio carrinhos;

    @Autowired
    private ReservasDeEstoque reservas;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        AGORA.set(INICIO);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfVen003_irParaPagamentoLevaItensEReservasAoCheckout() throws Exception {
        adicionar(ANA, 7, 2).andExpect(status().isOk());
        adicionar(ANA, 1, 1).andExpect(status().isOk());

        long checkoutId = idDe(escrever(post("/api/me/checkout"), ANA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itens[*].livroId", contains(7, 1)))
                .andExpect(jsonPath("$.subtotal").exists())
                .andExpect(jsonPath("$.quantidadeDeItens").value(3))
                .andExpect(jsonPath("$.expiraEm").value("2026-10-24T16:00:00Z")));

        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$.itens", hasSize(0)))
                .andExpect(jsonPath("$.expiraEm").doesNotExist());
        mvc.perform(get("/api/me/checkout").cookie(sessaoDe(ANA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(checkoutId));
        assertThat(reservas.listarPorOrigem(OrigemReserva.CARRINHO, carrinhoId())).isEmpty();
        assertThat(reservas.listarPorOrigem(OrigemReserva.CHECKOUT, checkoutId))
                .extracting(Reserva::getLivroId, Reserva::getQuantidade, Reserva::getExpiraEm)
                .containsExactlyInAnyOrder(tuple(7L, 2, INICIO.plusHours(1)),
                        tuple(1L, 1, INICIO.plusHours(1)));
        mvc.perform(get("/api/livros/7")).andExpect(jsonPath("$.disponivel").value(4));
    }

    @Test
    void dec12_umCheckoutAbertoPorCliente() throws Exception {
        adicionar(ANA, 7, 1).andExpect(status().isOk());
        escrever(post("/api/me/checkout"), ANA).andExpect(status().isCreated());
        adicionar(ANA, 1, 1).andExpect(status().isOk());

        escrever(post("/api/me/checkout"), ANA).andExpect(status().isConflict());
    }

    @Test
    void rfVen003_carrinhoVazioOuInexistenteNaoVaiParaPagamento() throws Exception {
        escrever(post("/api/me/checkout"), BRUNO)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RF-VEN-003")));
        adicionar(BRUNO, 1, 1).andExpect(status().isOk());
        escrever(delete("/api/me/carrinho/itens/1"), BRUNO).andExpect(status().isOk());

        escrever(post("/api/me/checkout"), BRUNO).andExpect(status().isUnprocessableContent());
        mvc.perform(get("/api/me/checkout").cookie(sessaoDe(BRUNO))).andExpect(status().isNotFound());
    }

    @Test
    void rnVen001_carrinhoVencidoSoVaiAoPagamentoSeAindaHouverEstoque() throws Exception {
        adicionar(ANA, 7, 6).andExpect(status().isOk());
        AGORA.set(INICIO.plusMinutes(31));
        adicionar(BRUNO, 7, 6).andExpect(status().isOk());

        escrever(post("/api/me/checkout"), ANA)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RN-VEN-001")));
        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA))).andExpect(jsonPath("$.itens", hasSize(1)));
    }

    @Test
    void dec12_voltarAoCarrinhoSomaOsItensEReservaDeNovo() throws Exception {
        adicionar(ANA, 7, 2).andExpect(status().isOk());
        long checkoutId = idDe(escrever(post("/api/me/checkout"), ANA).andExpect(status().isCreated()));
        AGORA.set(INICIO.plusMinutes(10));
        adicionar(ANA, 7, 1).andExpect(status().isOk());

        escrever(post("/api/me/checkout/voltar"), ANA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].livroId", contains(7)))
                .andExpect(jsonPath("$.itens[0].quantidade").value(3))
                .andExpect(jsonPath("$.expiraEm").value("2026-10-24T15:40:00Z"));

        mvc.perform(get("/api/me/checkout").cookie(sessaoDe(ANA))).andExpect(status().isNotFound());
        assertThat(reservas.listarPorOrigem(OrigemReserva.CHECKOUT, checkoutId)).isEmpty();
        assertThat(reservas.listarPorOrigem(OrigemReserva.CARRINHO, carrinhoId()))
                .extracting(Reserva::getQuantidade).containsExactly(3);
        escrever(post("/api/me/checkout/voltar"), ANA).andExpect(status().isNotFound());
    }

    @Test
    void rnVen014_checkoutAvisaEExpiraLevandoOsItensAosRemovidos() throws Exception {
        adicionar(ANA, 7, 6).andExpect(status().isOk());
        long checkoutId = idDe(escrever(post("/api/me/checkout"), ANA).andExpect(status().isCreated()));

        AGORA.set(INICIO.plusMinutes(55));
        expirarReservas.executar();
        notificacoesDe(ANA).andExpect(jsonPath("$.itens[*].tipo", contains("CHECKOUT_EXPIRANDO")));

        AGORA.set(INICIO.plusMinutes(60));
        expirarReservas.executar();

        mvc.perform(get("/api/me/checkout").cookie(sessaoDe(ANA))).andExpect(status().isNotFound());
        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$.itens", hasSize(0)))
                .andExpect(jsonPath("$.removidos[*].livroId", contains(7)))
                .andExpect(jsonPath("$.removidos[0].quantidade").value(6));
        assertThat(reservas.listarPorOrigem(OrigemReserva.CHECKOUT, checkoutId)).isEmpty();
        notificacoesDe(ANA).andExpect(jsonPath("$.itens[*].tipo", hasItem("CHECKOUT_EXPIRADO")));
        adicionar(BRUNO, 7, 6).andExpect(status().isOk());
    }

    @Test
    void dec12_inativarAContaEncerraOCheckoutELiberaAsReservas() throws Exception {
        adicionar(ANA, 7, 6).andExpect(status().isOk());
        long checkoutId = idDe(escrever(post("/api/me/checkout"), ANA).andExpect(status().isCreated()));

        escrever(post("/api/me/inativar"), ANA).andExpect(status().isNoContent());

        assertThat(reservas.listarPorOrigem(OrigemReserva.CHECKOUT, checkoutId)).isEmpty();
        mvc.perform(get("/api/livros/7")).andExpect(jsonPath("$.disponivel").value(6));
    }

    @Test
    void rnVen014_checkoutVencidoAntesDaTarefaNaoBloqueiaUmNovo() throws Exception {
        adicionar(ANA, 7, 2).andExpect(status().isOk());
        escrever(post("/api/me/checkout"), ANA).andExpect(status().isCreated());
        AGORA.set(INICIO.plusMinutes(61));

        mvc.perform(get("/api/me/checkout").cookie(sessaoDe(ANA))).andExpect(status().isNotFound());
        adicionar(ANA, 1, 1).andExpect(status().isOk());
        escrever(post("/api/me/checkout"), ANA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itens[*].livroId", contains(1)));
        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$.removidos[*].livroId", contains(7)));
    }

    @Test
    void rnfVen001_livroReadicionadoNoCarrinhoNaoViraRemovidoNaExpiracaoDoCheckout() throws Exception {
        adicionar(ANA, 7, 2).andExpect(status().isOk());
        escrever(post("/api/me/checkout"), ANA).andExpect(status().isCreated());
        AGORA.set(INICIO.plusMinutes(45));
        adicionar(ANA, 7, 1).andExpect(status().isOk());

        AGORA.set(INICIO.plusMinutes(60));
        expirarReservas.executar();

        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$.itens[*].livroId", contains(7)))
                .andExpect(jsonPath("$.removidos", hasSize(0)));
    }

    @Test
    void rnVen001_voltarSemEstoqueParaASomaExplicaComoSair() throws Exception {
        adicionar(ANA, 7, 4).andExpect(status().isOk());
        escrever(post("/api/me/checkout"), ANA).andExpect(status().isCreated());
        adicionar(ANA, 7, 2).andExpect(status().isOk());
        // A reserva do carrinho vence e Bruno leva as duas unidades.
        AGORA.set(INICIO.plusMinutes(31));
        adicionar(BRUNO, 7, 2).andExpect(status().isOk());

        escrever(post("/api/me/checkout/voltar"), ANA)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-001"))
                .andExpect(jsonPath("$.violacoes[0].mensagem").value(containsString(
                        "Diminua ou remova no carrinho")));
        mvc.perform(get("/api/me/checkout").cookie(sessaoDe(ANA))).andExpect(status().isOk());
    }

    private long carrinhoId() {
        return carrinhos.buscarPorCliente(CLIENTE_ANA).orElseThrow().getId();
    }

    private static long idDe(ResultActions resposta) throws Exception {
        String corpo = resposta.andReturn().getResponse().getContentAsString();
        return Long.parseLong(corpo.replaceAll("(?s).*\"id\"\\s*:\\s*(\\d+).*", "$1"));
    }

    private ResultActions notificacoesDe(long usuarioId) throws Exception {
        return mvc.perform(get("/api/me/notificacoes").cookie(sessaoDe(usuarioId))).andExpect(status().isOk());
    }

    private ResultActions adicionar(long usuarioId, long livroId, int quantidade) throws Exception {
        return escrever(post("/api/me/carrinho/itens"), usuarioId,
                "{\"livroId\":%d,\"quantidade\":%d}".formatted(livroId, quantidade));
    }

    private ResultActions escrever(MockHttpServletRequestBuilder requisicao, long usuarioId) throws Exception {
        return escrever(requisicao, usuarioId, "");
    }

    private ResultActions escrever(MockHttpServletRequestBuilder requisicao, long usuarioId, String corpo)
            throws Exception {
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrf).isNotNull();
        MockHttpServletRequestBuilder comSessao = requisicao.cookie(sessaoDe(usuarioId), csrf)
                .header("X-XSRF-TOKEN", csrf.getValue());
        return mvc.perform(corpo.isEmpty() ? comSessao
                : comSessao.contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Cookie sessaoDe(long usuarioId) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
