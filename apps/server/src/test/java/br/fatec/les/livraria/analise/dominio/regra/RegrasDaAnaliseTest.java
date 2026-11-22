package br.fatec.les.livraria.analise.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.analise.dominio.modelo.SolicitacaoDeAnalise;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class RegrasDaAnaliseTest {

    private static final LocalDate MARCO = LocalDate.of(2025, 3, 15);

    @Test
    void rfAna002_fimNaoPodeSerAnteriorAoInicio() {
        FimNaoAnteriorAoInicio regra = new FimNaoAnteriorAoInicio();

        assertThat(violacoes(regra, MARCO, MARCO.minusDays(1), List.of(1L)))
                .extracting(Violacao::codigo)
                .containsExactly("RF-ANA-002");
        assertThat(violacoes(regra, MARCO, MARCO, List.of(1L))).isEmpty();
    }

    @Test
    void rnAna002_periodoTemDe1A24MesesContandoOInicialEOFinal() {
        PeriodoDeAte24Meses regra = new PeriodoDeAte24Meses();

        // O mesmo mês, mesmo em dias diferentes, é um mês; o limite é de meses tocados, não de dias.
        assertThat(violacoes(regra, LocalDate.of(2025, 3, 1), MARCO, List.of(1L))).isEmpty();
        assertThat(violacoes(regra, MARCO, LocalDate.of(2027, 2, 1), List.of(1L))).isEmpty();
        assertThat(violacoes(regra, MARCO, LocalDate.of(2027, 3, 1), List.of(1L)))
                .extracting(Violacao::codigo)
                .containsExactly("RN-ANA-002");
        // O fim anterior ao início é do RF-ANA-002, não desta regra.
        assertThat(violacoes(regra, MARCO, MARCO.minusMonths(2), List.of(1L))).isEmpty();
    }

    @Test
    void rnAna002_anosBissextosENumerosDeDiasDiferentesNaoMudamAContagemDeMeses_DEC_19() {
        PeriodoDeAte24Meses regra = new PeriodoDeAte24Meses();

        // 29/02/2024 a 28/02/2025 tem 366 dias, mas é exatamente um ano (12 meses).
        assertThat(violacoes(regra, LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 28), List.of(1L))).isEmpty();
        // 01/02/2025 a 28/02/2025 também é um mês, apesar de ter três dias a menos.
        assertThat(violacoes(regra, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28), List.of(1L))).isEmpty();
        // O 25º mês calendário tocado, ainda que com poucos dias, é recusado.
        assertThat(violacoes(regra, LocalDate.of(2024, 2, 29), LocalDate.of(2026, 3, 1), List.of(1L)))
                .extracting(Violacao::codigo)
                .containsExactly("RN-ANA-002");
        // Dois dias em meses extremos já contam dois meses, mesmo com menos de 48 horas de intervalo.
        assertThat(
                        new SolicitacaoDeAnalise(LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 1), List.of(1L))
                                .quantidadeDeMeses())
                .isEqualTo(2);
    }

    @Test
    void rfAna003_exigeAoMenosUmaCategoria() {
        CategoriaSelecionada regra = new CategoriaSelecionada();

        assertThat(violacoes(regra, MARCO, MARCO, null)).extracting(Violacao::codigo).containsExactly("RF-ANA-003");
        assertThat(violacoes(regra, MARCO, MARCO, List.of())).hasSize(1);
        assertThat(violacoes(regra, MARCO, MARCO, List.of(1L, 2L))).isEmpty();
    }

    private static List<Violacao> violacoes(
            Regra<SolicitacaoDeAnalise> regra, LocalDate inicio, LocalDate fim, List<Long> categorias) {
        Violacoes violacoes = new Violacoes();
        regra.validar(new SolicitacaoDeAnalise(inicio, fim, categorias), violacoes);
        return violacoes.lista();
    }
}
