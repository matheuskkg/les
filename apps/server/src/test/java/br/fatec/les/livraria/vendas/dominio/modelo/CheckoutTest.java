package br.fatec.les.livraria.vendas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** RF-VEN-003, RN-VEN-014, RNF-VEN-001, DEC-12: regras da sessão de compra. */
class CheckoutTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final Duration PRAZO = Duration.ofMinutes(60);
    private static final Duration AVISO = Duration.ofMinutes(5);

    @Test
    void rfVen003_iniciaAbertoComPrazoContadoDoInicio() {
        Checkout checkout = novo();

        assertThat(checkout.aberto()).isTrue();
        assertThat(checkout.getIniciadoEm()).isEqualTo(INICIO);
        assertThat(checkout.getExpiraEm()).isEqualTo(INICIO.plusMinutes(60));
        assertThatThrownBy(() -> Checkout.iniciar(1L, List.of(), INICIO, PRAZO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rnVen014_avisaUmaVezEVenceNoPrazo() {
        Checkout checkout = novo();
        LocalDateTime fim = checkout.getExpiraEm();

        assertThat(checkout.deveAvisarEm(fim.minusMinutes(6), AVISO)).isFalse();
        assertThat(checkout.deveAvisarEm(fim.minusMinutes(5), AVISO)).isTrue();
        checkout.registrarAviso(fim.minusMinutes(5));
        assertThat(checkout.deveAvisarEm(fim.minusMinutes(4), AVISO)).isFalse();

        assertThat(checkout.venceuEm(fim.minusSeconds(1))).isFalse();
        assertThat(checkout.venceuEm(fim)).isTrue();
    }

    @Test
    void rnfVen001_itensDoCheckoutExpiradoVaoParaOsRemovidos() {
        Checkout checkout = novo();

        checkout.expirar();

        assertThat(checkout.getSituacao()).isEqualTo(SituacaoCheckout.EXPIRADO);
        assertThat(checkout.venceuEm(INICIO.plusHours(2))).isFalse();
        assertThat(checkout.itensRemovidosEm(INICIO.plusHours(1)))
                .containsExactly(new ItemRemovidoCarrinho(10L, 2, INICIO.plusHours(1)));
    }

    @Test
    void dec12_soOCheckoutAbertoPodeSerEncerradoOuExpirado() {
        Checkout checkout = novo();
        checkout.encerrar();

        assertThat(checkout.getSituacao()).isEqualTo(SituacaoCheckout.ENCERRADO);
        assertThatThrownBy(checkout::encerrar).isInstanceOf(ExcecaoDeConflito.class);
        assertThatThrownBy(checkout::expirar).isInstanceOf(ExcecaoDeConflito.class);
        assertThat(checkout.deveAvisarEm(checkout.getExpiraEm().minusMinutes(1), AVISO)).isFalse();
    }

    private static Checkout novo() {
        return Checkout.iniciar(1L, List.of(new ItemCheckout(10L, 2)), INICIO, PRAZO);
    }
}
