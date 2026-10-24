package br.fatec.les.livraria.vendas.infraestrutura.frete;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.frete.VolumeDoFrete;

/** RF-VEN-004, DEC-05.3: base da região mais R$ 2,00 por bloco extra, sobre a soma dos itens. */
class CalculadoraFreteSimuladaTest {

    private final CalculadoraFreteSimulada calculadora = new CalculadoraFreteSimulada();

    @Test
    void rfVen004_exemploDoDec053() {
        // 2 livros de 0,4 kg (cúbico 0,3 kg) para 01310-100 → 0,8 kg → 2 blocos → R$ 12,00 + R$ 2,00
        Dinheiro frete = calculadora.calcular("01310100", List.of(volume("0.400", "20", "15", "6", 2)));

        assertThat(frete).isEqualTo(Dinheiro.de("14.00"));
    }

    @Test
    void rfVen004_somaOsItensAntesDosBlocos() {
        // 0,3 + 0,3 = 0,6 kg → 2 blocos (cada item sozinho teria 1); CEP 6xxxx: R$ 30,00 + R$ 2,00
        Dinheiro frete = calculadora.calcular("69005040",
                List.of(volume("0.300", "1", "1", "1", 1), volume("0.300", "1", "1", "1", 1)));

        assertThat(frete).isEqualTo(Dinheiro.de("32.00"));
    }

    @Test
    void rfVen004_semItensNaoHaFrete() {
        assertThat(calculadora.calcular("01310100", List.of())).isEqualTo(Dinheiro.ZERO);
    }

    private static VolumeDoFrete volume(String peso, String altura, String largura, String profundidade,
            int quantidade) {
        return new VolumeDoFrete(new BigDecimal(peso), new BigDecimal(altura), new BigDecimal(largura),
                new BigDecimal(profundidade), quantidade);
    }
}
