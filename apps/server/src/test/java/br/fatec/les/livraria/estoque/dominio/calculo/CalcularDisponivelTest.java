package br.fatec.les.livraria.estoque.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Disponível = estoque − reservas ativas, nunca negativo (DEC-15.3, RN-EST-006). */
class CalcularDisponivelTest {

    private final CalcularDisponivel calcular = new CalcularDisponivel();

    @Test
    void dec153_descontaAsReservasAtivasDoSaldoEmMaos() {
        assertThat(calcular.calcular(20, 5)).isEqualTo(15);
    }

    @Test
    void dec153_disponivelNuncaFicaNegativo() {
        assertThat(calcular.calcular(3, 10)).isZero();
    }

    @Test
    void semReservaODisponivelEhOProprioSaldo() {
        assertThat(calcular.calcular(0, 0)).isZero();
        assertThat(calcular.calcular(7, 0)).isEqualTo(7);
    }

    @Test
    void recusaQuantidadesNegativas() {
        assertThatThrownBy(() -> calcular.calcular(-1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calcular.calcular(1, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
