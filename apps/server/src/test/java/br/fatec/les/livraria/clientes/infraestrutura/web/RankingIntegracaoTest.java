package br.fatec.les.livraria.clientes.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.clientes.dominio.ranking.CompraDoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;
import jakarta.servlet.http.Cookie;

/**
 * Ranking sobre o seed histórico (RN-CLI-007, DEC-04, DEC-12, DEC-14). Os valores esperados vêm do seed
 * {@code 400_pedidos_historicos.sql}: em 2026-10-24, dentro do período do seed, Carla (cliente 3) e Elisa
 * (cliente 5) têm mais de R$ 1.000,00, mais de 8 pedidos e compra nos últimos 30 dias, então 100. Em 2027-10-01 a
 * janela só pega o fim do seed: Carla com 3 pedidos (R$ 380,33, o último há 347 dias) tem 52, e Elisa com 5
 * (R$ 685,69, há 346 dias), 68. Diego não comprou e tem 0.
 */
@SpringBootTest
@Transactional
class RankingIntegracaoTest {

    private static final AtomicReference<LocalDateTime> AGORA = new AtomicReference<>();

    @TestConfiguration
    static class RelogioAjustavel {

        @Bean
        @Primary
        Relogio relogioAjustavel() {
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
    private VendasConsultas vendas;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        AGORA.set(LocalDateTime.of(2026, 10, 24, 12, 0));
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rnCli007_rankingNaConsultaNoDetalheENoPerfil() throws Exception {
        mvc.perform(como(1L, get("/api/admin/clientes").param("cpf", "123.456.789-09")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("Carla Mendes"))
                .andExpect(jsonPath("$.itens[0].ranking").value(100));
        mvc.perform(como(1L, get("/api/admin/clientes/5")))
                .andExpect(jsonPath("$.ranking").value(100));
        mvc.perform(como(6L, get("/api/me")))
                .andExpect(jsonPath("$.ranking").value(100));
    }

    @Test
    void rnCli007_janelaDe365DiasAteADataDoCalculo() throws Exception {
        AGORA.set(LocalDateTime.of(2027, 10, 1, 9, 0));

        mvc.perform(como(1L, get("/api/admin/clientes/3"))).andExpect(jsonPath("$.ranking").value(52));
        mvc.perform(como(1L, get("/api/admin/clientes/5"))).andExpect(jsonPath("$.ranking").value(68));
    }

    @Test
    void rnCli007_clienteSemComprasTemRankingZero() throws Exception {
        mvc.perform(como(1L, get("/api/admin/clientes/4"))).andExpect(jsonPath("$.ranking").value(0));
    }

    @Test
    void rnCli007_valorDoPedidoDescontaOsCuponsDasTrocasConcluidasEIgnoraReprovados_DEC_12() {
        // Março de 2025 do seed: Elisa pagou R$ 144,00 + R$ 22,00 de frete e trocou uma Fundação de R$ 48,00;
        // o pedido REPROVADA da Carla não conta.
        LocalDateTime inicio = LocalDateTime.of(2025, 3, 12, 0, 0);
        List<CompraDoCliente> compras = vendas.comprasDosClientes(List.of(3L, 5L), inicio).get(5L);

        assertThat(compras).contains(new CompraDoCliente(LocalDateTime.of(2025, 3, 12, 14, 30), Dinheiro.de("118.00")));
        assertThat(vendas.comprasDosClientes(List.of(3L), inicio).get(3L))
                .noneMatch(compra -> compra.realizadoEm().equals(LocalDateTime.of(2025, 3, 25, 16, 45)));
    }

    private MockHttpServletRequestBuilder como(Long usuarioId, MockHttpServletRequestBuilder requisicao) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return requisicao.cookie(new Cookie(EmissorDeToken.COOKIE, token));
    }
}
