package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

/**
 * O que ainda está reservado para um dono, por livro, para a finalização revalidar o estoque (RN-VEN-002,
 * DEC-12). A consulta pré-filtra no banco as reservas cujo prazo já venceu, e a definição de "ativa" continua
 * no domínio: {@code Reserva.ativaEm} é aplicada ao resultado (RN-EST-006, RNF-GER-001).
 */
class ConsultarReservasAtivasTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    private final ReservasEmMemoria reservas = new ReservasEmMemoria();
    private final ConsultarReservasAtivas consultar = new ConsultarReservasAtivas(reservas, () -> AGORA);

    @Test
    void rnVen002_somaAsReservasAtivasDeCadaLivro() {
        reservas.salvar(Reserva.nova(1L, OrigemReserva.CHECKOUT, 10L, 2, AGORA.plusMinutes(5)));
        reservas.salvar(Reserva.nova(1L, OrigemReserva.CHECKOUT, 10L, 3, AGORA.plusMinutes(10)));
        reservas.salvar(Reserva.nova(2L, OrigemReserva.CHECKOUT, 10L, 1, AGORA.plusHours(1)));

        assertThat(consultar.executar(OrigemReserva.CHECKOUT, 10L))
                .containsOnly(Map.entry(1L, 5), Map.entry(2L, 1));
    }

    @Test
    void rnEst006_reservaVencidaNaoContaMesmoQueAindaEstejaNaOrigem() {
        reservas.salvar(Reserva.nova(1L, OrigemReserva.CARRINHO, 10L, 4, AGORA.minusMinutes(1)));
        reservas.salvar(Reserva.nova(2L, OrigemReserva.CARRINHO, 10L, 2, AGORA.plusMinutes(5)));

        assertThat(consultar.executar(OrigemReserva.CARRINHO, 10L)).containsOnly(Map.entry(2L, 2));
    }

    @Test
    void rnEst006_reservaQueVenceExatamenteAgoraNaoEAtiva() {
        reservas.salvar(Reserva.nova(1L, OrigemReserva.CHECKOUT, 10L, 4, AGORA));

        assertThat(consultar.executar(OrigemReserva.CHECKOUT, 10L)).isEmpty();
    }

    @Test
    void rnVen002_reservaDeOutroDonoOuOutraOrigemNaoEntra() {
        reservas.salvar(Reserva.nova(1L, OrigemReserva.CARRINHO, 11L, 4, AGORA.plusMinutes(5)));
        reservas.salvar(Reserva.nova(1L, OrigemReserva.CHECKOUT, 11L, 1, AGORA.plusMinutes(5)));

        assertThat(consultar.executar(OrigemReserva.CARRINHO, 10L)).isEmpty();
        assertThat(consultar.executar(OrigemReserva.CHECKOUT, 11L)).containsOnly(Map.entry(1L, 1));
    }

    @Test
    void rnVen002_donoSemReservaNaoGeraChave() {
        assertThat(consultar.executar(OrigemReserva.CARRINHO, 99L)).isEmpty();
    }
}
