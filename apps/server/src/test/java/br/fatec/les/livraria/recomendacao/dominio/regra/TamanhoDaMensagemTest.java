package br.fatec.les.livraria.recomendacao.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.EnvioDeMensagem;

import org.junit.jupiter.api.Test;

class TamanhoDaMensagemTest {

    private final TamanhoDaMensagem regra = new TamanhoDaMensagem();

    private Violacoes validar(String texto) {
        Violacoes violacoes = new Violacoes();
        regra.validar(new EnvioDeMensagem(texto, 0), violacoes);
        return violacoes;
    }

    @Test
    void rnfRec001_aceitaAte500Caracteres() {
        assertThat(validar("a".repeat(500)).vazia()).isTrue();
        assertThat(validar("📚".repeat(500)).vazia()).isTrue();
    }

    @Test
    void rnfRec001_recusaAcimaDe500Caracteres() {
        assertThat(validar("a".repeat(501)).lista()).extracting(Violacao::codigo).containsExactly("RNF-REC-001");
    }

    @Test
    void rnfRec001_recusaMensagemEmBranco() {
        assertThat(validar("   ").lista()).extracting(Violacao::mensagem).containsExactly("Escreva a mensagem.");
        assertThat(validar(null).vazia()).isFalse();
    }
}
