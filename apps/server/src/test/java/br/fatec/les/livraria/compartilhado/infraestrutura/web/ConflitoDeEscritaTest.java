package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ExemploListagemJpa;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ExemploListagemRepositorio;

/** Conflitos de escrita respondem 409 com a configuração real da aplicação (DEC-07, DEC-15.2). */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:conflito;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
        + "DB_CLOSE_DELAY=-1")
@ActiveProfiles("h2")
@Transactional
class ConflitoDeEscritaTest {

    @RestController
    static class ControllerDeConflito {

        private final ExemploListagemRepositorio exemplos;

        ControllerDeConflito(ExemploListagemRepositorio exemplos) {
            this.exemplos = exemplos;
        }

        @GetMapping("/api/exemplo/conflito/unicidade")
        void violarUnicidade() {
            exemplos.saveAndFlush(exemplo("primeiro"));
            exemplos.saveAndFlush(exemplo("primeiro"));
        }

        @GetMapping("/api/exemplo/conflito/dominio")
        void conflitar() {
            throw new ExcecaoDeConflito("O cupom promocional já foi usado por este cliente.");
        }

        @GetMapping("/api/exemplo/conflito/otimista")
        void conflitarOtimista() {
            throw new ObjectOptimisticLockingFailureException(ExemploListagemJpa.class, 1L);
        }

        private static ExemploListagemJpa exemplo(String codigo) {
            return new ExemploListagemJpa("Exemplo", codigo, null, "SP", BigDecimal.TEN, LocalDate.of(2026, 1, 1));
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        ControllerDeConflito controllerDeConflito(ExemploListagemRepositorio exemplos) {
            return new ControllerDeConflito(exemplos);
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
    void restricaoDeUnicidadeVira409SemVazarDetalhesDoBanco() throws Exception {
        mvc.perform(get("/api/exemplo/conflito/unicidade").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O registro já existe ou conflita com outro cadastro."));
    }

    @Test
    void conflitoDeDominioVira409() throws Exception {
        mvc.perform(get("/api/exemplo/conflito/dominio").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O cupom promocional já foi usado por este cliente."));
    }

    @Test
    void bloqueioOtimistaVira409ComMensagemPropria() throws Exception {
        mvc.perform(get("/api/exemplo/conflito/otimista").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O registro foi alterado por outra operação. Tente novamente."));
    }
}
