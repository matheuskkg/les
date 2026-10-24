package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;

class TratadorDeErrosTest {

    record Corpo(int quantidade) {
    }

    @RestController
    static class ControllerDeTeste {

        @GetMapping("/regras")
        String violarRegras() {
            throw new ExcecaoDeNegocio(List.of(
                    new Violacao("RN-VEN-003", "Apenas um cupom promocional por compra."),
                    new Violacao("RN-VEN-004", "Valor mínimo de R$ 10,00 por cartão.")));
        }

        @GetMapping("/parametro")
        String violarParametro() {
            throw new ParametroInvalido("tamanho", "O tamanho da página deve estar entre 1 e 100.");
        }

        @GetMapping("/conflito")
        String conflitar() {
            throw new ExcecaoDeConflito("O pedido não pode ser alterado no status atual.");
        }

        @GetMapping("/conflito-otimista")
        String conflitarOtimista() {
            throw new OptimisticLockingFailureException("versão desatualizada");
        }

        @GetMapping("/conflito-unico")
        String conflitarUnicidade() {
            throw new DataIntegrityViolationException("falha",
                    new SQLIntegrityConstraintViolationException("duplicate key value violates unique constraint", "23505"));
        }

        @GetMapping("/conflito-unico-spring")
        String conflitarUnicidadeDoSpring() {
            throw new DuplicateKeyException("já existe");
        }

        @GetMapping("/conflito-unico-hibernate")
        String conflitarUnicidadeDoHibernate() {
            throw new DataIntegrityViolationException("falha", new ConstraintViolationException("já existe",
                    new SQLException("unique constraint"), ConstraintKind.UNIQUE, "uk_exemplo"));
        }

        @GetMapping("/integridade")
        String falharIntegridade() {
            throw new DataIntegrityViolationException("falha", new SQLException("check constraint", "23514"));
        }

        @PostMapping("/corpo")
        String receber(@RequestBody Corpo corpo) {
            return "ok";
        }

        @GetMapping("/falha")
        String falhar() {
            throw new IllegalStateException("detalhe interno");
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
            .setControllerAdvice(new TratadorDeErros())
            .build();

    @Test
    void respondeComStatus422ETodasAsViolacoes() throws Exception {
        mvc.perform(get("/regras"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.violacoes.length()").value(2))
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-003"))
                .andExpect(jsonPath("$.violacoes[1].mensagem").value("Valor mínimo de R$ 10,00 por cartão."));
    }

    @Test
    void respondeComStatus400ParaParametroInvalido() throws Exception {
        mvc.perform(get("/parametro"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.parametro").value("tamanho"))
                .andExpect(jsonPath("$.detail").value("O tamanho da página deve estar entre 1 e 100."));
    }

    @Test
    void respondeComStatus409ParaConflitoDeDominio() throws Exception {
        mvc.perform(get("/conflito"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O pedido não pode ser alterado no status atual."));
    }

    @Test
    void respondeComStatus409ParaAlteracaoConcorrente() throws Exception {
        mvc.perform(get("/conflito-otimista"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O registro foi alterado por outra operação. Tente novamente."));
    }

    @Test
    void respondeComStatus409ParaRestricaoDeUnicidade() throws Exception {
        mvc.perform(get("/conflito-unico"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O registro já existe ou conflita com outro cadastro."));
    }

    @Test
    void respondeComStatus409ParaDuplicateKeyException() throws Exception {
        mvc.perform(get("/conflito-unico-spring"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O registro já existe ou conflita com outro cadastro."));
    }

    @Test
    void respondeComStatus409ParaUnicidadeDoHibernate() throws Exception {
        mvc.perform(get("/conflito-unico-hibernate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O registro já existe ou conflita com outro cadastro."));
    }

    @Test
    void respondeComStatus500ParaOutraViolacaoDeIntegridadeSemExporDetalhes() throws Exception {
        mvc.perform(get("/integridade"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Erro inesperado no servidor."));
    }

    @Test
    void respondeComStatus400QuandoOJsonEhMalformado() throws Exception {
        mvc.perform(post("/corpo").contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void respondeComStatus500SemExporDetalhesInternos() throws Exception {
        mvc.perform(get("/falha"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Erro inesperado no servidor."));
    }
}
