package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
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
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ExpirarReservas;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import jakarta.servlet.http.Cookie;

/**
 * Expiração do carrinho sobre o seed (RN-VEN-014, RN-VEN-015, RNF-VEN-001, DEC-10, DEC-13): prazo de 30 min e aviso
 * 5 min antes. Ana é o usuário 2 (cliente 1) e Bruno o 3. Livro 7 (O Silmarillion) tem 6 em estoque. A tarefa é
 * executada diretamente, com o relógio do teste.
 */
@SpringBootTest
@Transactional
class ExpiracaoDoCarrinhoIntegracaoTest {

    private static final long ANA = 2L;
    private static final long CLIENTE_ANA = 1L;
    private static final long BRUNO = 3L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final LocalDateTime PRAZO = INICIO.plusMinutes(30);
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
    void rnVen014_avisaUmaVezCincoMinutosAntesDoPrazo() throws Exception {
        adicionar(ANA, 7, 2).andExpect(status().isOk());

        AGORA.set(PRAZO.minusMinutes(6));
        expirarReservas.executar();
        notificacoesDe(ANA).andExpect(jsonPath("$.itens", hasSize(0)));

        AGORA.set(PRAZO.minusMinutes(5));
        expirarReservas.executar();
        AGORA.set(PRAZO.minusMinutes(4));
        expirarReservas.executar();

        notificacoesDe(ANA)
                .andExpect(jsonPath("$.itens[*].tipo", contains("CARRINHO_EXPIRANDO")))
                .andExpect(jsonPath("$.itens[0].recurso").value("carrinho"));
        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA))).andExpect(jsonPath("$.itens", hasSize(1)));
    }

    @Test
    void rnVen015_vencidoOPrazoRetiraOsItensLiberaAsReservasENotifica() throws Exception {
        adicionar(ANA, 7, 6).andExpect(status().isOk());
        adicionar(ANA, 1, 2).andExpect(status().isOk());

        AGORA.set(PRAZO);
        expirarReservas.executar();

        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$.itens", hasSize(0)))
                .andExpect(jsonPath("$.expiraEm").doesNotExist())
                .andExpect(jsonPath("$.removidos[*].livroId", contains(7, 1)))
                .andExpect(jsonPath("$.removidos[0].titulo").value("O Silmarillion"))
                .andExpect(jsonPath("$.removidos[0].quantidade").value(6));
        assertThat(reservas.listarPorOrigem(OrigemReserva.CARRINHO, carrinhoDaAna().getId())).isEmpty();
        // Vencido sem aviso prévio, o cliente recebe só a expiração.
        notificacoesDe(ANA).andExpect(jsonPath("$.itens[*].tipo", contains("CARRINHO_EXPIRADO")));
        adicionar(BRUNO, 7, 6).andExpect(status().isOk());
    }

    @Test
    void rnVen014_prazoRenovadoNaoExpira() throws Exception {
        adicionar(ANA, 7, 1).andExpect(status().isOk());
        AGORA.set(PRAZO.minusMinutes(1));
        adicionar(ANA, 1, 1).andExpect(status().isOk());

        AGORA.set(PRAZO.plusMinutes(1));
        expirarReservas.executar();

        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.removidos", hasSize(0)));
    }

    @Test
    void rnfVen001_readicionarOuDispensarApagaOAvisoDeRemovido() throws Exception {
        adicionar(ANA, 7, 1).andExpect(status().isOk());
        adicionar(ANA, 1, 1).andExpect(status().isOk());
        AGORA.set(PRAZO);
        expirarReservas.executar();

        adicionar(ANA, 7, 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].livroId", contains(7)))
                .andExpect(jsonPath("$.removidos[*].livroId", contains(1)));

        escrever(delete("/api/me/carrinho/removidos/1"), ANA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.removidos", hasSize(0)));
        escrever(delete("/api/me/carrinho/removidos/1"), ANA).andExpect(status().isNotFound());
    }

    @Test
    void dec10_inativarAContaEsvaziaOCarrinhoELiberaAsReservas() throws Exception {
        adicionar(ANA, 7, 6).andExpect(status().isOk());
        Long carrinhoId = carrinhoDaAna().getId();

        escrever(post("/api/me/inativar"), ANA).andExpect(status().isNoContent());

        assertThat(carrinhoDaAna().getItens()).isEmpty();
        assertThat(reservas.listarPorOrigem(OrigemReserva.CARRINHO, carrinhoId)).isEmpty();
        mvc.perform(get("/api/livros/7")).andExpect(jsonPath("$.disponivel").value(6));
    }

    private Carrinho carrinhoDaAna() {
        return carrinhos.buscarPorCliente(CLIENTE_ANA).orElseThrow();
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
