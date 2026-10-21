package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;

class TratadorDeErrosTest {

    @RestController
    static class ControllerQueViolaRegras {

        @GetMapping("/teste")
        String executar() {
            throw new ExcecaoDeNegocio(List.of(
                    new Violacao("RN-VEN-003", "Apenas um cupom promocional por compra."),
                    new Violacao("RN-VEN-004", "Valor mínimo de R$ 10,00 por cartão.")));
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ControllerQueViolaRegras())
            .setControllerAdvice(new TratadorDeErros())
            .build();

    @Test
    void respondeComStatus422ETodasAsViolacoes() throws Exception {
        mvc.perform(get("/teste"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes.length()").value(2))
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-VEN-003"))
                .andExpect(jsonPath("$.violacoes[1].mensagem").value("Valor mínimo de R$ 10,00 por cartão."));
    }
}
