package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
