package br.fatec.les.livraria.vendas.aplicacao;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/** RN-VEN-014, DEC-05.1: o aviso de expiração é positivo e sai antes do fim do prazo. */
class PropriedadesDoCarrinhoTest {

    @Test
    void rnVen014_avisoPositivoEMenorQueOPrazo() {
        assertThatCode(() -> new PropriedadesDoCarrinho(Duration.ofMinutes(30), Duration.ofMinutes(5)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> new PropriedadesDoCarrinho(Duration.ofMinutes(30), Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PropriedadesDoCarrinho(Duration.ofMinutes(30), Duration.ofMinutes(30)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
