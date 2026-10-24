package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ConversorDePaginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ExemploListagemJpa;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ExemploListagemRepositorio;

/**
 * Listagem de exemplo com a configuração real de MVC e springdoc (DEC-07): os parâmetros aparecem na especificação
 * e os valores inválidos respondem 400 em ProblemDetail. O controller só existe neste contexto de teste.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:listagem-openapi;MODE=PostgreSQL;"
        + "DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@ActiveProfiles("h2")
class ListagemNoOpenApiTest {

    @RestController
    static class ControllerDeListagem {

        private final ExemploListagemRepositorio exemplos;

        ControllerDeListagem(ExemploListagemRepositorio exemplos) {
            this.exemplos = exemplos;
        }

        @GetMapping("/api/exemplo/listagem")
        Pagina<String> listar(
                @Listagem(campos = { "nome", "codigo" }, padrao = "nome,asc") Paginacao paginacao) {
            return ConversorDePaginacao.paraPagina(
                    exemplos.findAll(Specification.unrestricted(), ConversorDePaginacao.paraPageable(paginacao)),
                    ExemploListagemJpa::getCodigo);
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        ControllerDeListagem controllerDeListagem(ExemploListagemRepositorio exemplos) {
            return new ControllerDeListagem(exemplos);
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void aceitaPaginaTamanhoEOrdemRepetida() throws Exception {
        mvc.perform(get("/api/exemplo/listagem")
                .param("pagina", "1")
                .param("tamanho", "2")
                .param("ordem", "nome,desc")
                .param("ordem", "codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray())
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(2))
                .andExpect(jsonPath("$.totalItens").value(0))
                .andExpect(jsonPath("$.totalPaginas").value(0));
    }

    @Test
    void especificacaoMostraOsParametrosDeListagem() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters.length()").value(3))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[*].name",
                        hasItems("pagina", "tamanho", "ordem")))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'pagina')]"
                        + ".schema.minimum").value(hasItem(0)))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'pagina')]"
                        + ".schema.default").value(hasItem(0)))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'tamanho')]"
                        + ".schema.minimum").value(hasItem(1)))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'tamanho')]"
                        + ".schema.maximum").value(hasItem(100)))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'tamanho')]"
                        + ".schema.default").value(hasItem(20)))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'ordem')]"
                        + ".schema.type").value(hasItem("array")))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'ordem')]"
                        + ".description").value(hasItem(containsString("nome, codigo"))))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'ordem')]"
                        + ".description").value(hasItem(containsString("Padrão: nome,asc"))))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'ordem')]"
                        + ".schema.example").isEmpty())
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'ordem')]"
                        + ".explode").value(hasItem(true)))
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.parameters[?(@.name == 'paginacao')]")
                        .isEmpty())
                .andExpect(jsonPath("$.components.schemas.Paginacao").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/exemplo/listagem'].get.requestBody").doesNotExist());
    }

    @Test
    void recusaCampoDeOrdenacaoNaoDeclarado() throws Exception {
        mvc.perform(get("/api/exemplo/listagem").param("ordem", "senha,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.parametro").value("ordem"))
                .andExpect(jsonPath("$.detail").value(containsString("senha")));
    }

    @Test
    void recusaPaginaETamanhoInvalidos() throws Exception {
        mvc.perform(get("/api/exemplo/listagem").param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parametro").value("pagina"))
                .andExpect(jsonPath("$.detail").value("A página não pode ser negativa."));

        mvc.perform(get("/api/exemplo/listagem").param("tamanho", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parametro").value("tamanho"))
                .andExpect(jsonPath("$.detail").value("O tamanho da página deve estar entre 1 e 100."));

        mvc.perform(get("/api/exemplo/listagem").param("pagina", String.valueOf(Integer.MAX_VALUE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parametro").value("pagina"));
    }

    @Test
    void recusaOrdemMalformada() throws Exception {
        mvc.perform(get("/api/exemplo/listagem").param("ordem", "nome"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parametro").value("ordem"))
                .andExpect(jsonPath("$.detail").value("Ordenação inválida: use o formato campo,asc|desc."));
    }

    @Test
    void recusaPaginaNaoNumerica() throws Exception {
        mvc.perform(get("/api/exemplo/listagem").param("pagina", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parametro").value("pagina"))
                .andExpect(jsonPath("$.detail").value("O parâmetro pagina deve ser um número inteiro."));
    }
}
