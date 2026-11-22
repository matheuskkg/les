package br.fatec.les.livraria.catalogo.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/** Preço de venda: maior custo × (1 + margem), arredondado só no fim (RF-EST-002, RN-EST-002, DEC-15.1). */
class CalcularPrecoVendaTest {

    private final CalcularPrecoVenda calcularPrecoVenda = new CalcularPrecoVenda();

    @Test
    void rfEst002_custoDe3333ComMargemDe15PorCentoDa3833() {
        // Exemplo do DEC-15.1: 33.33 × 1.15 = 38.3295 → 38.33.
        Dinheiro preco = calcularPrecoVenda.calcular(new BigDecimal("33.33"), new BigDecimal("0.15"));

        assertThat(preco).isEqualTo(Dinheiro.de("38.33"));
    }

    @Test
    void rnLiv003_usaAMargemDoGrupoDePrecificacaoComoFracao() {
        // Os grupos são gravados como fração: 0.2000 representa 20%.
        Dinheiro preco = calcularPrecoVenda.calcular(new BigDecimal("39.90"), new BigDecimal("0.20"));

        assertThat(preco).isEqualTo(Dinheiro.de("47.88"));
    }

    @Test
    void dec15_1_arredondaMeioCentavoParaCima() {
        // 1.00 × 1.005 = 1.005 → 1.01, e não 1.00 como no HALF_EVEN.
        Dinheiro preco = calcularPrecoVenda.calcular(new BigDecimal("1.00"), new BigDecimal("0.005"));

        assertThat(preco).isEqualTo(Dinheiro.de("1.01"));
    }

    @Test
    void dec15_1_naoArredondaValoresIntermediarios() {
        // 10.00 × 1.3333 = 13.333 → 13.33. Arredondar a margem para 0.33 antes daria 13.30.
        Dinheiro preco = calcularPrecoVenda.calcular(new BigDecimal("10.00"), new BigDecimal("0.3333"));

        assertThat(preco).isEqualTo(Dinheiro.de("13.33"));
    }

    @Test
    void rfEst002_margemZeroMantemOCusto() {
        Dinheiro preco = calcularPrecoVenda.calcular(new BigDecimal("42.00"), BigDecimal.ZERO);

        assertThat(preco).isEqualTo(Dinheiro.de("42.00"));
    }

    @Test
    void rfEst002_resultadoSempreComDuasCasas() {
        Dinheiro preco = calcularPrecoVenda.calcular(new BigDecimal("29.90"), new BigDecimal("0.35"));

        assertThat(preco.valor().scale()).isEqualTo(2);
        assertThat(preco).isEqualTo(Dinheiro.de("40.37"));
    }

    @Test
    void rejeitaMaiorCustoNulo() {
        assertThatThrownBy(() -> calcularPrecoVenda.calcular(null, new BigDecimal("0.20")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maiorCusto");
    }

    @Test
    void rejeitaMargemNula() {
        assertThatThrownBy(() -> calcularPrecoVenda.calcular(new BigDecimal("39.90"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("margem");
    }

    @Test
    void rejeitaMaiorCustoZeroOuNegativo() {
        assertThatThrownBy(() -> calcularPrecoVenda.calcular(BigDecimal.ZERO, new BigDecimal("0.20")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maiorCusto");

        assertThatThrownBy(() -> calcularPrecoVenda.calcular(new BigDecimal("-1.00"), new BigDecimal("0.20")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maiorCusto");
    }

    @Test
    void rejeitaMargemNegativa() {
        assertThatThrownBy(() -> calcularPrecoVenda.calcular(new BigDecimal("39.90"), new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("margem");
    }
}
