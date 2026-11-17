package br.fatec.les.livraria.analise.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import jakarta.servlet.http.Cookie;

/**
 * Análise de vendas do admin sobre o seed histórico (RF-ANA-001 a RF-ANA-004, RN-ANA-001 a RN-ANA-004, DEC-12,
 * DEC-15.4). Março de 2025 tem valores escritos à mão no {@code scripts/gerar_seed_historico.py}: dois Dom Casmurro
 * (Romance e História) com frete e promocional, três Fundação com uma unidade TROCADO, dois O Hobbit com uma unidade
 * em TROCA AUTORIZADA e um Código Limpo REPROVADA. Outubro de 2026 tem os pedidos 237, 238 e 241 APROVADA, o 242
 * EM TRANSPORTE e o 244 REPROVADA, para fixar a regra do RN-ANA-004: aprovada, em transporte e entregue contam;
 * reprovada não. Só meses passados são consultados, então os pedidos que outros testes gravam com o relógio real não
 * mudam os valores.
 */
@SpringBootTest
@Transactional
class AnaliseAdminControllerTest {

    private static final String ROTA = "/api/admin/analise";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private EmissorDeToken emissor;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rnAna001_somaPorCategoriaDescontandoTrocasConcluidasSemFreteNemReprovadas() throws Exception {
        mvc.perform(como(1L, marco(get(ROTA)).param("categoria", "1", "5", "2", "3", "4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inicio").value("2025-03"))
                .andExpect(jsonPath("$.fim").value("2025-03"))
                .andExpect(jsonPath("$.meses", contains("2025-03")))
                .andExpect(jsonPath("$.series[*].categoria",
                        contains("Romance", "História", "Ficção Científica", "Fantasia", "Tecnologia")))
                // RN-ANA-001 e DEC-12: valor cheio em cada categoria, sem frete e sem o desconto do promocional.
                .andExpect(jsonPath("$.series[0].valores", contains("95.76")))
                .andExpect(jsonPath("$.series[1].valores", contains("95.76")))
                // RN-ANA-004: 3 × 48,00 menos a unidade TROCADO.
                .andExpect(jsonPath("$.series[2].valores", contains("96.00")))
                // Troca ainda autorizada não desconta.
                .andExpect(jsonPath("$.series[3].valores", contains("80.00")))
                // RN-ANA-003 e RN-ANA-004: o único pedido do mês é REPROVADA, então a linha vale zero.
                .andExpect(jsonPath("$.series[4].valores", contains("0.00")));
    }

    @Test
    void rnAna004_contamAprovadaEmTransporteEEntregueNoMesmoMes() throws Exception {
        mvc.perform(como(1L, get(ROTA).param("inicio", "2026-10").param("fim", "2026-10")
                .param("categoria", "1", "2", "3", "4", "5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meses", contains("2026-10")))
                // Romance não tem pedido pendente em outubro; Ficção, Fantasia, Tecnologia e História somam os
                // pedidos APROVADA 237, 238 e 241 e o EM TRANSPORTE 242, e deixam o REPROVADA 244 de fora.
                .andExpect(jsonPath("$.series[0].valores", contains("84.17")))
                .andExpect(jsonPath("$.series[1].valores", contains("235.04")))
                .andExpect(jsonPath("$.series[2].valores", contains("198.54")))
                .andExpect(jsonPath("$.series[3].valores", contains("423.75")))
                .andExpect(jsonPath("$.series[4].valores", contains("353.28")));
    }

    @Test
    void rnAna003_umValorPorMesNaOrdemDoPeriodo() throws Exception {
        mvc.perform(como(1L, get(ROTA).param("inicio", "2025-03").param("fim", "2025-05").param("categoria", "2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meses", contains("2025-03", "2025-04", "2025-05")))
                .andExpect(jsonPath("$.series[0].categoriaId").value(2))
                .andExpect(jsonPath("$.series[0].valores.length()").value(3))
                .andExpect(jsonPath("$.series[0].valores[0]").value("96.00"));
    }

    @Test
    void rfAna002_rnAna002_rfAna003_periodoESelecaoInvalidosRespondem422() throws Exception {
        mvc.perform(como(1L, get(ROTA).param("inicio", "2025-03").param("fim", "2025-02").param("categoria", "1")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", contains("RF-ANA-002")));
        mvc.perform(como(1L, get(ROTA).param("inicio", "2024-11").param("fim", "2026-10").param("categoria", "1")))
                .andExpect(status().isOk());
        mvc.perform(como(1L, get(ROTA).param("inicio", "2024-11").param("fim", "2026-11").param("categoria", "1")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", contains("RN-ANA-002")));
        mvc.perform(como(1L, marco(get(ROTA))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", contains("RF-ANA-003")));
    }

    @Test
    void mesMalformadoAusenteOuCategoriaInexistenteResponde400_DEC_07() throws Exception {
        mvc.perform(como(1L, get(ROTA).param("inicio", "2025-13").param("fim", "2025-12").param("categoria", "1")))
                .andExpect(status().isBadRequest());
        mvc.perform(como(1L, get(ROTA).param("inicio", "2025-03").param("categoria", "1")))
                .andExpect(status().isBadRequest());
        mvc.perform(como(1L, marco(get(ROTA)).param("categoria", "999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parametro").value("categoria"));
    }

    @Test
    void soOAdminConsultaAAnalise() throws Exception {
        mvc.perform(como(2L, marco(get(ROTA)).param("categoria", "1"))).andExpect(status().isForbidden());
        mvc.perform(marco(get(ROTA)).param("categoria", "1")).andExpect(status().isUnauthorized());
    }

    @Test
    void rfAna004_exportaOsDadosDoGraficoEmCsv_DEC_15_4() throws Exception {
        byte[] csv = mvc.perform(como(1L, marco(get(ROTA + "/exportacao")).param("categoria", "2", "4")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"analise-de-vendas-2025-03-a-2025-03.csv\""))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(new String(csv, StandardCharsets.UTF_8)).isEqualTo("﻿" + """
                mes;categoria;valor\r
                2025-03;Ficção Científica;96,00\r
                2025-03;Tecnologia;0,00\r
                """);
        mvc.perform(como(1L, marco(get(ROTA + "/exportacao"))))
                .andExpect(status().isUnprocessableContent());
    }

    private static MockHttpServletRequestBuilder marco(MockHttpServletRequestBuilder requisicao) {
        return requisicao.param("inicio", "2025-03").param("fim", "2025-03");
    }

    private MockHttpServletRequestBuilder como(Long usuarioId, MockHttpServletRequestBuilder requisicao) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return requisicao.cookie(new Cookie(EmissorDeToken.COOKIE, token));
    }
}
