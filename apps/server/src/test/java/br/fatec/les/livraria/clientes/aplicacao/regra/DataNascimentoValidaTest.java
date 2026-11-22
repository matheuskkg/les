package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.comDataNascimento;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.relogio;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/** RN-CLI-006: data de nascimento plausível, contada a partir do relógio (2026-10-24 nos testes). */
class DataNascimentoValidaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 24);

    private final DataNascimentoValida regra = new DataNascimentoValida(relogio());

    @Test
    void rnCli006_dataDeHojeEhAceita() {
        assertThat(violacoes(comDataNascimento(HOJE), regra)).isEmpty();
    }

    @Test
    void rnCli006_dataNoFuturoEhRejeitada() {
        assertThat(violacoes(comDataNascimento(HOJE.plusDays(1)), regra))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-006");
                    assertThat(violacao.mensagem()).contains("futuro");
                });
    }

    @Test
    void rnCli006_dataDeExatamenteCentoETrintaAnosEhAceita() {
        assertThat(violacoes(comDataNascimento(HOJE.minusYears(130)), regra)).isEmpty();
    }

    @Test
    void rnCli006_dataDeMaisDeCentoETrintaAnosEhRejeitada() {
        assertThat(violacoes(comDataNascimento(HOJE.minusYears(130).minusDays(1)), regra))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-006");
                    assertThat(violacao.mensagem()).contains("130");
                });
    }

    @Test
    void rnCli006_dataAusenteNaoEhReportadaPorEstaRegra() {
        assertThat(violacoes(comDataNascimento(null), regra)).isEmpty();
    }
}
