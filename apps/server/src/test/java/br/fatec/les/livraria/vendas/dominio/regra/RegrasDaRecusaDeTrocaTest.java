package br.fatec.les.livraria.vendas.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.RecusaDeTroca;

class RegrasDaRecusaDeTrocaTest {

    @Test
    void rfVen012_recusaExigeMotivo() {
        MotivoDaRecusaInformado regra = new MotivoDaRecusaInformado();

        assertThat(violacoes(regra, null)).extracting(Violacao::codigo).containsExactly("RF-VEN-012");
        assertThat(violacoes(regra, "   ")).hasSize(1);
        assertThat(violacoes(regra, "Livro com marcas de uso")).isEmpty();
    }

    @Test
    void rfVen012_motivoTemAte300Caracteres() {
        MotivoDaRecusaNoLimite regra = new MotivoDaRecusaNoLimite();

        assertThat(violacoes(regra, "a".repeat(300))).isEmpty();
        assertThat(violacoes(regra, " " + "a".repeat(300) + " ")).isEmpty();
        assertThat(violacoes(regra, "a".repeat(301))).extracting(Violacao::codigo).containsExactly("RF-VEN-012");
        assertThat(violacoes(regra, null)).isEmpty();
    }

    private static List<Violacao> violacoes(Regra<RecusaDeTroca> regra, String motivo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(new RecusaDeTroca(motivo), violacoes);
        return violacoes.lista();
    }
}
