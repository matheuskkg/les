package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDeEstoque;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDoLivro;
import br.fatec.les.livraria.estoque.dominio.calculo.CalcularDisponivel;

/** Disponibilidade calculada em lote sobre um único retrato de saldos e reservas (DEC-15.3, RF-LIV-005). */
class ConsultarDisponibilidadeTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @Test
    void devolveZeroParaLivroSemLinhaDeSaldo() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of());
        ConsultarDisponibilidade consulta = consulta(saldo);

        assertThat(consulta.executar(List.of(99L))).containsEntry(99L, 0);
    }

    @Test
    void descontaAsReservasAtivasDoSaldo() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of(
                1L, new SaldoDoLivro(20, 5),
                2L, new SaldoDoLivro(5, 8)));
        ConsultarDisponibilidade consulta = consulta(saldo);

        assertThat(consulta.executar(List.of(1L, 2L)))
                .containsEntry(1L, 15)
                .containsEntry(2L, 0);
    }

    @Test
    void consultaSaldosComOInstanteDoRelogioEmUmaUnicaChamada() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of(
                1L, new SaldoDoLivro(10, 2),
                2L, new SaldoDoLivro(4, 0)));
        ConsultarDisponibilidade consulta = consulta(saldo);

        consulta.executar(List.of(1L, 2L));

        assertThat(saldo.chamadas).isEqualTo(1);
        assertThat(saldo.idsRecebidos).containsExactly(1L, 2L);
        assertThat(saldo.instanteConsultado).isEqualTo(AGORA);
    }

    @Test
    void consultaVaziaNaoAcionaAPorta() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of());
        ConsultarDisponibilidade consulta = consulta(saldo);

        assertThat(consulta.executar(List.of())).isEmpty();
        assertThat(saldo.chamadas).isZero();
    }

    @Test
    void consultaComElementoNuloEhRejeitada() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of());
        ConsultarDisponibilidade consulta = consulta(saldo);

        assertThatThrownBy(() -> consulta.executar(Arrays.asList(1L, null)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("null");
        assertThat(saldo.chamadas).isZero();
    }

    private static ConsultarDisponibilidade consulta(SaldoDeEstoque saldo) {
        Relogio relogio = () -> AGORA;
        return new ConsultarDisponibilidade(saldo, new CalcularDisponivel(), relogio);
    }

    private static final class SaldoDeEstoqueFake implements SaldoDeEstoque {

        private final Map<Long, SaldoDoLivro> saldos;
        private final List<Long> idsRecebidos = new ArrayList<>();
        private int chamadas;
        private LocalDateTime instanteConsultado;

        private SaldoDeEstoqueFake(Map<Long, SaldoDoLivro> saldos) {
            this.saldos = saldos;
        }

        @Override
        public Map<Long, SaldoDoLivro> saldos(Collection<Long> livroIds, LocalDateTime agora) {
            chamadas++;
            idsRecebidos.addAll(livroIds);
            instanteConsultado = agora;
            return saldos.entrySet().stream()
                    .filter(entrada -> livroIds.contains(entrada.getKey()))
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
