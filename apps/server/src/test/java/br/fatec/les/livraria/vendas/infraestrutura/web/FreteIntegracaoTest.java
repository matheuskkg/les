package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
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
import jakarta.servlet.http.Cookie;

/**
 * Frete do checkout para o endereço de entrega escolhido (RF-VEN-004, RF-VEN-005, DEC-05.3, DEC-10) sobre o seed. Ana
 * é o usuário 2, com os endereços 1 e 2 (CEPs de SP); o endereço 3 é do Bruno. Dom Casmurro (livro 1) custa R$ 47,88
 * e pesa 0,32 kg, acima do peso cúbico.
 */
@SpringBootTest
@Transactional
class FreteIntegracaoTest {

    private static final long ANA = 2L;
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

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        AGORA.set(INICIO);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfVen004_freteParaEnderecoCadastrado() throws Exception {
        iniciarCheckout(1, 2);

        // 2 × 0,32 kg = 0,64 kg → 2 blocos; CEP 01310-100 (SP): R$ 12,00 + R$ 2,00
        calcular("{\"entrega\":{\"enderecoId\":2}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frete").value("14.00"))
                .andExpect(jsonPath("$.subtotal").value("95.76"))
                .andExpect(jsonPath("$.total").value("109.76"));
    }

    @Test
    void rfVen005_freteParaEnderecoNovoSemGravarNoPerfil() throws Exception {
        iniciarCheckout(1, 1);

        calcular("{\"entrega\":{\"novo\":" + enderecoNovo("69005-040") + "}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frete").value("30.00"))
                .andExpect(jsonPath("$.total").value("77.88"));
        mvc.perform(get("/api/me/enderecos").cookie(sessaoDe(ANA))).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void rfVen005_enderecoDeOutroClienteNaoEhEncontrado() throws Exception {
        iniciarCheckout(1, 1);

        calcular("{\"entrega\":{\"enderecoId\":3}}").andExpect(status().isNotFound());
    }

    @Test
    void rfVen005_escolhaSemEnderecoOuComEnderecoNovoInvalido() throws Exception {
        iniciarCheckout(1, 1);

        calcular("{\"entrega\":{}}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RF-VEN-005")));
        calcular("{}").andExpect(status().isUnprocessableContent());
        calcular("{\"entrega\":{\"enderecoId\":1,\"novo\":" + enderecoNovo("01415-000") + "}}")
                .andExpect(status().isUnprocessableContent());
        calcular("{\"entrega\":{\"novo\":" + enderecoNovo("123") + "}}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RN-CLI-003")));
    }

    @Test
    void rfVen004_semCheckoutAbertoNaoHaFrete() throws Exception {
        calcular("{\"entrega\":{\"enderecoId\":1}}").andExpect(status().isNotFound());

        iniciarCheckout(1, 1);
        AGORA.set(INICIO.plusMinutes(61));
        calcular("{\"entrega\":{\"enderecoId\":1}}").andExpect(status().isNotFound());
    }

    private void iniciarCheckout(long livroId, int quantidade) throws Exception {
        escrever(post("/api/me/carrinho/itens"), "{\"livroId\":%d,\"quantidade\":%d}".formatted(livroId, quantidade))
                .andExpect(status().isOk());
        escrever(post("/api/me/checkout"), "").andExpect(status().isCreated());
    }

    private ResultActions calcular(String corpo) throws Exception {
        return escrever(post("/api/me/checkout/frete"), corpo);
    }

    private static String enderecoNovo(String cep) {
        return """
                {"nomeCurto":"Presente","tipoEndereco":"CASA","tipoLogradouro":"RUA","logradouro":"Rua Ramos Ferreira",\
                "numero":"10","bairro":"Centro","cep":"%s","cidade":"Manaus","uf":"AM"}""".formatted(cep);
    }

    private ResultActions escrever(MockHttpServletRequestBuilder requisicao, String corpo) throws Exception {
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrf).isNotNull();
        MockHttpServletRequestBuilder comSessao = requisicao.cookie(sessaoDe(ANA), csrf)
                .header("X-XSRF-TOKEN", csrf.getValue());
        return mvc.perform(corpo.isEmpty() ? comSessao
                : comSessao.contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Cookie sessaoDe(long usuarioId) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
