package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Build do web servido pelo Spring (DEC-09), com o {@code static/index.html} de {@code src/test/resources}. */
@SpringBootTest
class ConfiguracaoDoWebTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void rotaDoWebDevolveOIndex() throws Exception {
        mvc.perform(get("/me/pedidos/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("web de teste")));
    }

    @Test
    void arquivoExistenteEhServido() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk());
    }

    @Test
    void arquivoInexistenteDa404() throws Exception {
        mvc.perform(get("/assets/inexistente.js")).andExpect(status().isNotFound());
    }

    @Test
    void rotaInexistenteDaApiDa404EmProblemDetail() throws Exception {
        mvc.perform(get("/api/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
