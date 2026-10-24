package br.fatec.les.livraria.analise.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.analise.dominio.modelo.SolicitacaoDeAnalise;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

class RegrasDaAnaliseTest {

    private static final YearMonth MARCO = YearMonth.of(2025, 3);

    @Test
    void rfAna002_fimNaoPodeSerAnteriorAoInicio() {
        FimNaoAnteriorAoInicio regra = new FimNaoAnteriorAoInicio();

        assertThat(violacoes(regra, MARCO, MARCO.minusMonths(1), List.of(1L))).extracting(Violacao::codigo)
                .containsExactly("RF-ANA-002");
        assertThat(violacoes(regra, MARCO, MARCO, List.of(1L))).isEmpty();
    }

    @Test
    void rnAna002_periodoTemDe1A24MesesContandoOInicialEOFinal() {
        PeriodoDeAte24Meses regra = new PeriodoDeAte24Meses();

        assertThat(violacoes(regra, MARCO, MARCO, List.of(1L))).isEmpty();
        assertThat(violacoes(regra, MARCO, MARCO.plusMonths(23), List.of(1L))).isEmpty();
        assertThat(violacoes(regra, MARCO, MARCO.plusMonths(24), List.of(1L))).extracting(Violacao::codigo)
                .containsExactly("RN-ANA-002");
        // O fim anterior ao início é do RF-ANA-002, não desta regra.
        assertThat(violacoes(regra, MARCO, MARCO.minusMonths(30), List.of(1L))).isEmpty();
    }

    @Test
    void rfAna003_exigeAoMenosUmaCategoria() {
        CategoriaSelecionada regra = new CategoriaSelecionada();

        assertThat(violacoes(regra, MARCO, MARCO, null)).extracting(Violacao::codigo).containsExactly("RF-ANA-003");
        assertThat(violacoes(regra, MARCO, MARCO, List.of())).hasSize(1);
        assertThat(violacoes(regra, MARCO, MARCO, List.of(1L, 2L))).isEmpty();
    }

    private static List<Violacao> violacoes(Regra<SolicitacaoDeAnalise> regra, YearMonth inicio, YearMonth fim,
            List<Long> categorias) {
        Violacoes violacoes = new Violacoes();
        regra.validar(new SolicitacaoDeAnalise(inicio, fim, categorias), violacoes);
        return violacoes.lista();
    }
}
