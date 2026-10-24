package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.CupomPromocionalJpaRepository;
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.UsoCupomPromocionalJpa;
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.UsoCupomPromocionalJpaRepository;
import jakarta.servlet.http.Cookie;

/**
 * Conferência do pagamento sobre o seed (RF-VEN-006, RF-VEN-007, RN-VEN-003 a RN-VEN-007, DEC-11, DEC-15.2). Ana é o
 * usuário 2 (cliente 1), com o cartão Visa 1 e os cupons de troca 1 (R$ 20), 2 (R$ 40) e 3 (R$ 35); o 4 está usado e
 * o 5, vencido. Bruno é o usuário 3. Um Dom Casmurro (R$ 47,88) para o endereço 1 (SP, frete R$ 12,00) dá R$ 59,88.
 */
@SpringBootTest
@Transactional
class PagamentoIntegracaoTest {

    private static final long ANA = 2L;
    private static final long BRUNO = 3L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final AtomicReference<LocalDateTime> AGORA = new AtomicReference<>(INICIO);
    private static final String ENTREGA = "\"entrega\":{\"enderecoId\":1}";

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
    private CupomPromocionalJpaRepository promocionais;

    @Autowired
    private UsoCupomPromocionalJpaRepository usos;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        AGORA.set(INICIO);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfVen007_listaSoOsCuponsDeTrocaDisponiveisDoCliente() throws Exception {
        mvc.perform(get("/api/me/cupons-de-troca").cookie(sessaoDe(ANA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", contains("TROCA-ANA-20", "TROCA-ANA-40", "TROCA-ANA-35")))
                .andExpect(jsonPath("$[0].valor").value("20.00"));
        mvc.perform(get("/api/me/cupons-de-troca").cookie(sessaoDe(BRUNO)))
                .andExpect(jsonPath("$[*].codigo", contains("TROCA-BRUNO-15")));
    }

    @Test
    void rfVen007_semCartoesMostraORestanteDepoisDoPromocional() throws Exception {
        iniciarCheckout(ANA);

        conferir(ANA, "{" + ENTREGA + ",\"cuponsPromocionais\":[\" bemvindo10 \"]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value("47.88"))
                .andExpect(jsonPath("$.frete").value("12.00"))
                .andExpect(jsonPath("$.total").value("59.88"))
                .andExpect(jsonPath("$.descontoPromocional").value("10.00"))
                .andExpect(jsonPath("$.restante").value("49.88"))
                .andExpect(jsonPath("$.cartoes", hasSize(0)));
    }

    @Test
    void rnVen006_cuponsDesnecessariosSaoRecusadosEOExcedenteViraTroco() throws Exception {
        iniciarCheckout(ANA);

        // R$ 95 em cupons para R$ 59,88: sem o de R$ 20 ou sem o de R$ 35, os demais ainda cobrem
        conferir(ANA, "{" + ENTREGA + ",\"cuponsDeTroca\":[1,2,3]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes", hasSize(2)))
                .andExpect(jsonPath("$.violacoes[*].codigo", everyItem(is("RN-VEN-006"))))
                .andExpect(jsonPath("$.violacoes[*].mensagem", hasItem(containsString("TROCA-ANA-20"))))
                .andExpect(jsonPath("$.violacoes[*].mensagem", hasItem(containsString("TROCA-ANA-35"))));
        conferir(ANA, "{" + ENTREGA + ",\"cuponsDeTroca\":[1,2]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuponsDeTroca[*].abatido", contains("20.00", "39.88")))
                .andExpect(jsonPath("$.troco").value("0.12"))
                .andExpect(jsonPath("$.restante").value("0.00"));
    }

    @Test
    void rnVen004_cartoesFechamORestanteComAoMenosDezReaisCada() throws Exception {
        iniciarCheckout(ANA);

        conferir(ANA, "{" + ENTREGA + ",\"cartoes\":[{\"cartaoId\":1,\"valor\":59.88}]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes[0].cartaoId").value(1))
                .andExpect(jsonPath("$.cartoes[0].bandeira").value("Visa"))
                .andExpect(jsonPath("$.cartoes[0].numero").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.cartoes[0].valor").value("59.88"));
        conferir(ANA, "{" + ENTREGA + ",\"cartoes\":[{\"cartaoId\":1,\"valor\":9.88},"
                + "{\"novo\":" + cartaoNovo("2030-12") + ",\"valor\":50.00}]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", contains("RN-VEN-004")));
        conferir(ANA, "{" + ENTREGA + ",\"cartoes\":[{\"cartaoId\":1,\"valor\":50.00}]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].mensagem").value("A soma dos cartões deve ser R$ 59,88."));
        conferir(ANA, "{" + ENTREGA + ",\"cartoes\":[{\"cartaoId\":1,\"valor\":29.94},{\"cartaoId\":1,\"valor\":29.94}]}")
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void rnVen005_comCuponsUmCartaoPagaORestanteAbaixoDeDez() throws Exception {
        iniciarCheckout(ANA);

        // R$ 59,88 − R$ 20 − R$ 35 = R$ 4,88 no cartão novo
        conferir(ANA, "{" + ENTREGA + ",\"cuponsDeTroca\":[1,3],\"cartoes\":[{\"novo\":" + cartaoNovo("2030-12")
                + ",\"valor\":4.88}]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restante").value("4.88"))
                .andExpect(jsonPath("$.cartoes[0].cartaoId").doesNotExist())
                .andExpect(jsonPath("$.cartoes[0].bandeira").value("Mastercard"));
    }

    @Test
    void rnVen007_promocionalDesconhecidoVencidoAbaixoDoMinimoOuJaUsado() throws Exception {
        iniciarCheckout(ANA);

        conferir(ANA, "{" + ENTREGA + ",\"cuponsPromocionais\":[\"NAOEXISTE\"]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-007"));
        conferir(ANA, "{" + ENTREGA + ",\"cuponsPromocionais\":[\"NATAL2025\"]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].mensagem").value("O cupom NATAL2025 está vencido."));
        conferir(ANA, "{" + ENTREGA + ",\"cuponsPromocionais\":[\"LIVROS15\"]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].mensagem", containsString("R$ 100,00")));
        conferir(ANA, "{" + ENTREGA + ",\"cuponsPromocionais\":[\"FRETE5\",\"FRETEGRATIS\"]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-003"));

        UsoCupomPromocionalJpa uso = new UsoCupomPromocionalJpa();
        uso.setCupomPromocional(promocionais.findByCodigo("FRETEGRATIS").orElseThrow());
        uso.setClienteId(1L);
        uso.setPedidoId(999L);
        usos.saveAndFlush(uso);
        conferir(ANA, "{" + ENTREGA + ",\"cuponsPromocionais\":[\"FRETEGRATIS\"]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].mensagem").value("Você já usou o cupom FRETEGRATIS."));
    }

    @Test
    void rnVen007_cupomDeTrocaUsadoVencidoOuDeOutroCliente() throws Exception {
        iniciarCheckout(ANA);
        iniciarCheckout(BRUNO);

        conferir(ANA, "{" + ENTREGA + ",\"cuponsDeTroca\":[4,5]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes", hasSize(2)));
        conferir(ANA, "{" + ENTREGA + ",\"cuponsDeTroca\":[1,1]}").andExpect(status().isUnprocessableContent());
        conferir(BRUNO, "{\"entrega\":{\"enderecoId\":3},\"cuponsDeTroca\":[1]}").andExpect(status().isNotFound());
    }

    @Test
    void rfVen006_cartaoNovoVencidoOuDeOutroClienteNaoEhAceito() throws Exception {
        iniciarCheckout(ANA);

        conferir(ANA, "{" + ENTREGA + ",\"cartoes\":[{\"novo\":" + cartaoNovo("2026-09") + ",\"valor\":59.88}]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RN-CLI-004")));
        conferir(ANA, "{" + ENTREGA + ",\"cartoes\":[{\"cartaoId\":2,\"valor\":59.88}]}")
                .andExpect(status().isNotFound());
        conferir(ANA, "{" + ENTREGA + ",\"cartoes\":[{\"valor\":59.88}]}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RF-VEN-006"));
    }

    @Test
    void rfVen007_semCheckoutAbertoNaoHaPagamento() throws Exception {
        conferir(ANA, "{" + ENTREGA + "}").andExpect(status().isNotFound());
    }

    private void iniciarCheckout(long usuarioId) throws Exception {
        escrever(post("/api/me/carrinho/itens"), usuarioId, "{\"livroId\":1,\"quantidade\":1}")
                .andExpect(status().isOk());
        escrever(post("/api/me/checkout"), usuarioId, "").andExpect(status().isCreated());
    }

    private ResultActions conferir(long usuarioId, String corpo) throws Exception {
        return escrever(post("/api/me/checkout/pagamento"), usuarioId, corpo);
    }

    private static String cartaoNovo(String validade) {
        return """
                {"numero":"5555 5555 5555 4444","nomeImpresso":"ANA SOUZA","bandeiraId":2,"validade":"%s"}"""
                .formatted(validade);
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
