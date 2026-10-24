package br.fatec.les.livraria.acesso.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.TrocaDeSenha;
import br.fatec.les.livraria.acesso.dominio.modelo.SenhaInformada;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/** RNF-CLI-001, RF-CLI-008: a nova senha é obrigatória na alteração de senha. */
class NovaSenhaObrigatoriaTest {

    private final NovaSenhaObrigatoria regra = new NovaSenhaObrigatoria();

    @Test
    void rnfCli001_novaSenhaInformadaNaoTemViolacao() {
        assertThat(violacoes(new TrocaDeSenha("Atual@123", "Nova@123", "Nova@123"))).isEmpty();
    }

    @Test
    void rnfCli001_novaSenhaEmBrancoViolaARegra() {
        assertThat(violacoes(new TrocaDeSenha("Atual@123", "   ", "   ")))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RNF-CLI-001");
                    assertThat(violacao.mensagem()).isEqualTo("Informe a nova senha.");
                });
    }

    @Test
    void rnfCli001_novaSenhaNulaViolaARegra() {
        assertThat(violacoes(new TrocaDeSenha("Atual@123", null, null))).hasSize(1);
    }

    private Violacoes validar(SenhaInformada alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes);
        return violacoes;
    }

    private java.util.List<br.fatec.les.livraria.compartilhado.dominio.Violacao> violacoes(SenhaInformada alvo) {
        return validar(alvo).lista();
    }
}
