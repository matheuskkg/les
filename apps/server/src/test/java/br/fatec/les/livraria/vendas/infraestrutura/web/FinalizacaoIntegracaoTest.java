package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.AprovarPedido;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ExpirarReservas;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.CupomJpa;
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.CupomJpaRepository;
import jakarta.servlet.http.Cookie;

/**
 * "Finalizar compra" de ponta a ponta (RF-VEN-008, RN-VEN-002, RN-VEN-006, RN-VEN-007, RN-VEN-008, RF-EST-003,
 * RNF-GER-002, DEC-05.2, DEC-11, DEC-12, DEC-13, DEC-15.2). Sem {@code @Transactional}: a finalização faz commit em
 * duas transações, e a auditoria só grava no commit. Por isso cada teste cadastra um cliente novo, com cupons próprios,
 * e o saldo do livro é restaurado no fim. Um Dom Casmurro (R$ 47,88) para São Paulo (frete R$ 12,00) dá R$ 59,88.
 */
@SpringBootTest
class FinalizacaoIntegracaoTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final AtomicReference<LocalDateTime> AGORA = new AtomicReference<>(INICIO);
    private static final AtomicInteger CLIENTES = new AtomicInteger();
    private static final long LIVRO = 1L;

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
    private CupomJpaRepository cupons;

    @Autowired
    private PedidoRepositorio pedidos;

    @Autowired
    private ExpirarReservas expirarReservas;

    @Autowired
    private AprovarPedido aprovarPedido;

    @Autowired
    private JdbcClient jdbc;

    private MockMvc mvc;
    private int saldoInicial;

    @BeforeEach
    void configurar() {
        AGORA.set(INICIO);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        saldoInicial = saldo();
    }

    @AfterEach
    void restaurarSaldo() {
        jdbc.sql("update estoque set quantidade = ? where livro_id = ?").params(saldoInicial, LIVRO).update();
    }

    @Test
    void rfVen008_aprovadaBaixaOEstoqueConcluiOCheckoutENotifica() throws Exception {
        Cliente ana = novoCliente();
        long checkoutId = iniciarCheckout(ana);

        String resposta = finalizar(ana, cartaoSalvo(ana, "49.88", "123"), "\"cuponsPromocionais\":[\"BEMVINDO10\"]")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.total").value("49.88"))
                .andExpect(jsonPath("$.recusas", hasSize(0)))
                .andReturn().getResponse().getContentAsString();
        long pedidoId = ((Number) JsonPath.read(resposta, "$.pedidoId")).longValue();

        assertThat(saldo()).isEqualTo(saldoInicial - 1);
        assertThat(jdbc.sql("select count(*) from reserva where origem = 'CHECKOUT' and origem_id = ?")
                .param(checkoutId).query(Long.class).single()).isZero();
        mvc.perform(get("/api/me/checkout").cookie(sessaoDe(ana.usuarioId()))).andExpect(status().isNotFound());
        assertThat(statusAuditados(pedidoId)).containsExactly("EM_PROCESSAMENTO", "APROVADA");
        assertThat(tiposNotificados(ana.usuarioId())).contains("PEDIDO_APROVADO");
        assertThat(tiposNotificados(1L)).contains("PEDIDO_AGUARDANDO_DESPACHO");

        Map<String, Object> copia = jdbc.sql("select cast(p.cliente as varchar) cliente, cast(i.livro as varchar) livro,"
                + " cast(c.cartao as varchar) cartao from pedido p join item_pedido i on i.pedido_id = p.id"
                + " join pagamento_cartao c on c.pedido_id = p.id where p.id = ?").param(pedidoId).query().singleRow();
        assertThat(copia.get("cliente").toString()).contains(ana.cpf());
        assertThat(copia.get("livro").toString()).contains("Dom Casmurro").contains("categorias");
        assertThat(copia.get("cartao").toString()).contains("1111").doesNotContain("4111111111111111")
                .doesNotContain("123");
    }

    @Test
    void rnVen008_reprovadaDevolveOsCuponsMantemOCheckoutEPermiteTentarDeNovo() throws Exception {
        Cliente ana = novoCliente();
        long cupom = cupomDeTroca(ana, "20.00");
        iniciarCheckout(ana);
        String cupons = "\"cuponsPromocionais\":[\"BEMVINDO10\"],\"cuponsDeTroca\":[" + cupom + "]";

        finalizar(ana, cartaoSalvo(ana, "29.88", "000"), cupons)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REPROVADA"))
                .andExpect(jsonPath("$.recusas[0].ultimosDigitos").value("1111"))
                .andExpect(jsonPath("$.recusas[0].motivo").value("Código de segurança inválido"));

        assertThat(this.cupons.findById(cupom).orElseThrow().getSituacao()).isEqualTo(SituacaoCupom.DISPONIVEL);
        assertThat(saldo()).isEqualTo(saldoInicial);
        assertThat(tiposNotificados(ana.usuarioId())).contains("PEDIDO_REPROVADO");
        mvc.perform(get("/api/me/checkout").cookie(sessaoDe(ana.usuarioId()))).andExpect(status().isOk());

        finalizar(ana, cartaoSalvo(ana, "29.88", "123"), cupons)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APROVADA"));
        assertThat(this.cupons.findById(cupom).orElseThrow().getSituacao()).isEqualTo(SituacaoCupom.USADO);
        assertThat(saldo()).isEqualTo(saldoInicial - 1);
    }

    @Test
    void rnVen006_cuponsAcimaDoTotalGeramCupomDeTroco() throws Exception {
        Cliente ana = novoCliente();
        long cupom = cupomDeTroca(ana, "70.00");
        iniciarCheckout(ana);

        String resposta = finalizar(ana, "[]", "\"cuponsDeTroca\":[" + cupom + "]")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andReturn().getResponse().getContentAsString();
        long pedidoId = ((Number) JsonPath.read(resposta, "$.pedidoId")).longValue();

        CupomJpa troco = this.cupons.findAll().stream()
                .filter(gerado -> gerado.getCodigo().equals("TROCO-" + pedidoId)).findFirst().orElseThrow();
        assertThat(troco.getValor()).isEqualByComparingTo("10.12");
        assertThat(troco.getClienteId()).isEqualTo(ana.clienteId());
        assertThat(troco.getSituacao()).isEqualTo(SituacaoCupom.DISPONIVEL);
    }

    @Test
    void rnVen007_pagamentoIncompletoOuSemCodigoNaoCriaPedido() throws Exception {
        Cliente ana = novoCliente();
        iniciarCheckout(ana);

        finalizar(ana, "[]", null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-004"));
        finalizar(ana, cartaoSalvo(ana, "59.88", "12"), null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-CLI-004"));
        assertThat(jdbc.sql("select count(*) from pedido where cliente_id = ?").param(ana.clienteId())
                .query(Long.class).single()).isZero();
    }

    @Test
    void dec13_pedidoEmProcessamentoBarraOutraFinalizacaoEEhReprovadoNaExpiracao() throws Exception {
        Cliente ana = novoCliente();
        long checkoutId = iniciarCheckout(ana);
        long pendente = pedidos.salvar(Pedidos.emProcessamento(checkoutId, ana.clienteId())).getId();

        finalizar(ana, cartaoSalvo(ana, "59.88", "123"), null).andExpect(status().isConflict());

        AGORA.set(INICIO.plusMinutes(61));
        expirarReservas.executar();

        assertThat(pedidos.buscarParaAlterar(pendente).orElseThrow().getStatus()).isEqualTo(StatusPedido.REPROVADA);
        assertThat(statusAuditados(pendente)).containsExactly("EM_PROCESSAMENTO", "REPROVADA");
        assertThat(saldo()).isEqualTo(saldoInicial);
    }

    @Test
    void dec13_aprovacaoDepoisDoPrazoReprovaSemBaixarOEstoque() throws Exception {
        Cliente ana = novoCliente();
        long checkoutId = iniciarCheckout(ana);
        Pedido pendente = pedidos.salvar(Pedidos.emProcessamento(checkoutId, ana.clienteId()));

        AGORA.set(INICIO.plusMinutes(61));
        Pedido resultado = aprovarPedido.executar(pendente, Dinheiro.ZERO);

        assertThat(resultado.getStatus()).isEqualTo(StatusPedido.REPROVADA);
        assertThat(saldo()).isEqualTo(saldoInicial);
    }

    private ResultActions finalizar(Cliente cliente, String cartoes, String cupons) throws Exception {
        String corpo = "{\"entrega\":{\"enderecoId\":%d},\"cobranca\":{\"enderecoId\":%d},\"cartoes\":%s%s}"
                .formatted(cliente.enderecoId(), cliente.enderecoId(), cartoes, cupons == null ? "" : "," + cupons);
        return escrever(post("/api/me/checkout/finalizar"), cliente.usuarioId(), corpo);
    }

    private static String cartaoSalvo(Cliente cliente, String valor, String codigo) {
        return "[{\"cartaoId\":%d,\"valor\":\"%s\",\"codigoSeguranca\":\"%s\"}]".formatted(cliente.cartaoId(), valor,
                codigo);
    }

    private long iniciarCheckout(Cliente cliente) throws Exception {
        escrever(post("/api/me/carrinho/itens"), cliente.usuarioId(), "{\"livroId\":1,\"quantidade\":1}")
                .andExpect(status().isOk());
        String checkout = escrever(post("/api/me/checkout"), cliente.usuarioId(), "")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(checkout, "$.id")).longValue();
    }

    private long cupomDeTroca(Cliente cliente, String valor) {
        CupomJpa cupom = new CupomJpa();
        cupom.setClienteId(cliente.clienteId());
        cupom.setCodigo("TROCA-TESTE-" + cliente.clienteId() + "-" + valor);
        cupom.setValor(new BigDecimal(valor));
        cupom.setSituacao(SituacaoCupom.DISPONIVEL);
        cupom.setGeradoEm(INICIO.minusDays(1));
        return cupons.saveAndFlush(cupom).getId();
    }

    /** Cadastra um cliente com CPF, e-mail e telefone únicos, um endereço em São Paulo e um Visa final 1111. */
    private Cliente novoCliente() throws Exception {
        int n = CLIENTES.incrementAndGet();
        String cpf = cpf(700000000 + n * 1013);
        String email = "finalizacao" + n + "@livraria.test";
        String corpo = """
                {
                  "nome": "Cliente Finalização", "genero": "FEMININO", "dataNascimento": "1990-01-01",
                  "cpf": "%s", "email": "%s",
                  "telefone": { "tipo": "CELULAR", "ddd": "11", "numero": "97%07d" },
                  "senha": "Senha@123", "confirmacaoSenha": "Senha@123",
                  "enderecos": [{ "nomeCurto": "Casa", "tipoEndereco": "CASA", "tipoLogradouro": "RUA",
                    "logradouro": "Rua Nova", "numero": "10", "bairro": "Centro", "cep": "01001000",
                    "cidade": "São Paulo", "uf": "SP" }],
                  "cartoes": [{ "numero": "4111111111111111", "nomeImpresso": "CLIENTE FINALIZACAO",
                    "validade": "2030-12" }]
                }
                """.formatted(cpf, email, n);
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        mvc.perform(post("/api/clientes").cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isCreated());
        long usuarioId = usuarios.buscarPorLogin(email).orElseThrow().getId();
        Cookie sessao = sessaoDe(usuarioId);
        String enderecos = mvc.perform(get("/api/me/enderecos").cookie(sessao)).andReturn().getResponse()
                .getContentAsString();
        String cartoes = mvc.perform(get("/api/me/cartoes").cookie(sessao)).andReturn().getResponse()
                .getContentAsString();
        long clienteId = jdbc.sql("select id from cliente where usuario_id = ?").param(usuarioId)
                .query(Long.class).single();
        return new Cliente(usuarioId, clienteId, cpf, ((Number) JsonPath.read(enderecos, "$[0].id")).longValue(),
                ((Number) JsonPath.read(cartoes, "$[0].id")).longValue());
    }

    private static String cpf(int base) {
        String digitos = "%09d".formatted(base);
        return digitos + digito(digitos) + digito(digitos + digito(digitos));
    }

    private static int digito(String digitos) {
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += (digitos.charAt(i) - '0') * (digitos.length() + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private int saldo() {
        return jdbc.sql("select quantidade from estoque where livro_id = ?").param(LIVRO).query(Integer.class)
                .single();
    }

    private List<String> statusAuditados(long pedidoId) {
        return jdbc.sql("select status from pedido_aud where id = ? order by revisao").param(pedidoId)
                .query(String.class).list();
    }

    private List<String> tiposNotificados(long usuarioId) {
        return jdbc.sql("select tipo from notificacao where usuario_id = ?").param(usuarioId).query(String.class)
                .list();
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

    private record Cliente(long usuarioId, long clienteId, String cpf, long enderecoId, long cartaoId) {
    }
}
