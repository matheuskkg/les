package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

class ReenvioSemRespostaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 25, 12, 0);
    private final ReenvioSemResposta regra = new ReenvioSemResposta();

    private static MensagemDaConversa doCliente(String texto) {
        return MensagemDaConversa.doCliente(1L, texto, AGORA);
    }

    private static MensagemDaConversa resposta() {
        return MensagemDaConversa.doAssistente(1L,
                new RespostaDoAssistente(TipoDeResposta.DUVIDA, "Pode repetir?", List.of()), 9L, AGORA);
    }

    @Test
    void rnfRec001_mesmoTextoDaUltimaMensagemSemRespostaEhReenvio() {
        assertThat(regra.reenvio(List.of(resposta(), doCliente("Hobbit")), "Hobbit")).isTrue();
    }

    @Test
    void rnfRec001_mensagemRespondidaOuTextoDiferenteNaoEhReenvio() {
        assertThat(regra.reenvio(List.of(doCliente("Hobbit"), resposta()), "Hobbit")).isFalse();
        assertThat(regra.reenvio(List.of(doCliente("Hobbit")), "Tolkien")).isFalse();
        assertThat(regra.reenvio(List.of(), "Hobbit")).isFalse();
    }
}
