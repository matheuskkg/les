package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.estoque.aplicacao.regra.DisponivelParaReservar;
import br.fatec.les.livraria.estoque.dominio.calculo.CalcularDisponivel;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

/** RN-VEN-001, RN-VEN-014, DEC-15.3: reserva com conferência do disponível e prazo. */
class ReservarTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final LocalDateTime PRAZO = AGORA.plusMinutes(30);
    private static final long LIVRO = 7L;

    private final Map<Long, Integer> estoque = new HashMap<>(Map.of(LIVRO, 6));
    private final ReservasEmMemoria reservas = new ReservasEmMemoria();
    private final Reservar reservar = new Reservar(reservas, new DisponivelParaReservar(
            new EstoqueEmMemoria(estoque), reservas, new CalcularDisponivel()), () -> AGORA);

    @Test
    void rnVen001_reservaDentroDoDisponivel() {
        reservar.executar(LIVRO, 4, OrigemReserva.CARRINHO, 1L, PRAZO);

        assertThat(reservas.buscar(LIVRO, OrigemReserva.CARRINHO, 1L)).get()
                .extracting(Reserva::getQuantidade).isEqualTo(4);
    }

    @Test
    void rnVen001_acimaDoDisponivelInformaQuantoHa() {
        reservar.executar(LIVRO, 4, OrigemReserva.CARRINHO, 1L, PRAZO);

        ExcecaoDeNegocio erro = catchThrowableOfType(ExcecaoDeNegocio.class,
                () -> reservar.executar(LIVRO, 3, OrigemReserva.CARRINHO, 2L, PRAZO));

        assertThat(erro.getViolacoes()).singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-VEN-001");
                    assertThat(violacao.mensagem()).contains("Disponível: 2");
                });
    }

    @Test
    void rnVen001_aReservaDoProprioDonoNaoContaDuasVezes() {
        reservar.executar(LIVRO, 4, OrigemReserva.CARRINHO, 1L, PRAZO);
        reservar.executar(LIVRO, 6, OrigemReserva.CARRINHO, 1L, PRAZO);

        assertThat(reservas.todas).singleElement().extracting(Reserva::getQuantidade).isEqualTo(6);
    }

    @Test
    void rnVen014_reservaVencidaNaoConsomeEstoque() {
        reservas.salvar(Reserva.nova(LIVRO, OrigemReserva.CARRINHO, 9L, 6, AGORA.minusMinutes(1)));

        reservar.executar(LIVRO, 6, OrigemReserva.CARRINHO, 1L, PRAZO);

        assertThat(reservas.buscar(LIVRO, OrigemReserva.CARRINHO, 1L)).isPresent();
    }

    @Test
    void dec15_3_livroSemLinhaDeEstoqueTemZeroDisponivel() {
        assertThatThrownBy(() -> reservar.executar(99L, 1, OrigemReserva.CARRINHO, 1L, PRAZO))
                .isInstanceOf(ExcecaoDeNegocio.class);
    }

    @Test
    void rnVen014_prazoPrecisaEstarNoFuturo() {
        assertThatThrownBy(() -> reservar.executar(LIVRO, 1, OrigemReserva.CARRINHO, 1L, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
