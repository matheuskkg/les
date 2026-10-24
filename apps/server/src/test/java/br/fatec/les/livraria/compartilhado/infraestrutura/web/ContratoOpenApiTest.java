package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Garante que {@code apps/web/openapi.json}, de onde o {@code web} gera os tipos, corresponde à API atual (DEC-07).
 * Para regravar o arquivo: {@code mvn test -Dtest=ContratoOpenApiTest -Dlivraria.openapi.gravar=true}.
 */
@SpringBootTest
class ContratoOpenApiTest {

    private static final Path ARQUIVO = Path.of("../web/openapi.json");

    @Autowired
    private WebApplicationContext contexto;

    @Test
    void especificacaoVersionadaEstaAtualizada() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        String atual = mvc.perform(get("/v3/api-docs")).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8) + "\n";

        if (Boolean.getBoolean("livraria.openapi.gravar")) {
            Files.createDirectories(ARQUIVO.getParent());
            Files.writeString(ARQUIVO, atual);
        }

        assertThat(ARQUIVO).as("rode com -Dlivraria.openapi.gravar=true para regravar").exists();
        assertThat(Files.readString(ARQUIVO)).as("rode com -Dlivraria.openapi.gravar=true para regravar")
                .isEqualTo(atual);
    }
}
