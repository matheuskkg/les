package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDeEstoque;
import br.fatec.les.livraria.estoque.dominio.calculo.CalcularDisponivel;

/** Disponibilidade calculada em lote sobre saldos e reservas (DEC-15.3, RF-LIV-005). */
class ConsultarDisponibilidadeTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @Test
    void devolveZeroParaLivroSemLinhaDeSaldo() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of(), Map.of());
        ConsultarDisponibilidade consulta = consulta(saldo);

        assertThat(consulta.executar(List.of(99L))).containsEntry(99L, 0);
    }

    @Test
    void descontaAsReservasAtivasDoSaldo() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(
                Map.of(1L, 20, 2L, 5),
                Map.of(1L, 5, 2L, 8));
        ConsultarDisponibilidade consulta = consulta(saldo);

        assertThat(consulta.executar(List.of(1L, 2L)))
                .containsEntry(1L, 15)
                .containsEntry(2L, 0);
    }

    @Test
    void consultaAsReservasComOInstanteDoRelogio() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of(1L, 10), Map.of(1L, 2));
        ConsultarDisponibilidade consulta = consulta(saldo);

        consulta.executar(List.of(1L));

        assertThat(saldo.instanteDasReservas).isEqualTo(AGORA);
    }

    @Test
    void consultaVaziaNaoAcionaAPorta() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of(), Map.of());
        ConsultarDisponibilidade consulta = consulta(saldo);

        assertThat(consulta.executar(List.of())).isEmpty();
        assertThat(saldo.chamadas).isZero();
    }

    private static ConsultarDisponibilidade consulta(SaldoDeEstoque saldo) {
        Relogio relogio = () -> AGORA;
        return new ConsultarDisponibilidade(saldo, new CalcularDisponivel(), relogio);
    }

    private static final class SaldoDeEstoqueFake implements SaldoDeEstoque {

        private final Map<Long, Integer> quantidades;
        private final Map<Long, Integer> reservas;
        private final List<Long> idsRecebidos = new ArrayList<>();
        private int chamadas;
        private LocalDateTime instanteDasReservas;

        private SaldoDeEstoqueFake(Map<Long, Integer> quantidades, Map<Long, Integer> reservas) {
            this.quantidades = quantidades;
            this.reservas = reservas;
        }

        @Override
        public Map<Long, Integer> quantidadesEmEstoque(Collection<Long> livroIds) {
            chamadas++;
            idsRecebidos.addAll(livroIds);
            return filtrar(quantidades, livroIds);
        }

        @Override
        public Map<Long, Integer> reservasAtivas(Collection<Long> livroIds, LocalDateTime agora) {
            chamadas++;
            instanteDasReservas = agora;
            return filtrar(reservas, livroIds);
        }

        private static Map<Long, Integer> filtrar(Map<Long, Integer> valores, Collection<Long> livroIds) {
            return valores.entrySet().stream()
                    .filter(entrada -> livroIds.contains(entrada.getKey()))
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
