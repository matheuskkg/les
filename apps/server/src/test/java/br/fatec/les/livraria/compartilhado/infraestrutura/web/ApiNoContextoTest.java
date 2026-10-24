package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;

/** Formatos do DEC-07 com a configuração real da aplicação (Jackson, springdoc e tratamento de erros). */
@SpringBootTest
class ApiNoContextoTest {

    record Oferta(String titulo, Dinheiro preco, YearMonth validade, @Nullable String observacao) {
    }

    @RestController
    static class ControllerDeTeste {

        @GetMapping("/api/exemplo/oferta")
        Oferta oferta() {
            return new Oferta("Livro", Dinheiro.de("10"), YearMonth.of(2027, 8), null);
        }

        @GetMapping("/api/exemplo/regras")
        Oferta violarRegras() {
            throw new ExcecaoDeNegocio("RN-VEN-004", "Valor mínimo de R$ 10,00 por cartão.");
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        ControllerDeTeste controllerDeTeste() {
            return new ControllerDeTeste();
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void jsonUsaDinheiroComoStringEOmiteNulos() throws Exception {
        mvc.perform(get("/api/exemplo/oferta").with(jwt()))
                .andExpect(jsonPath("$.preco").value("10.00"))
                .andExpect(jsonPath("$.validade").value("2027-08"))
                .andExpect(jsonPath("$.observacao").doesNotExist());
    }

    @Test
    void violacoesFicamNoTopoDoProblemDetail() throws Exception {
        mvc.perform(get("/api/exemplo/regras").with(jwt()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-004"))
                .andExpect(jsonPath("$.properties").doesNotExist());
    }

    @Test
    void especificacaoRefleteOFormatoDoJson() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.schemas.Oferta.properties.preco.type").value("string"))
                .andExpect(jsonPath("$.components.schemas.Oferta.properties.validade.type").value("string"))
                .andExpect(jsonPath("$.components.schemas.Oferta.required.length()").value(3))
                .andExpect(jsonPath("$.components.schemas.Oferta.required[?(@ == 'observacao')]").isEmpty());
    }
}
