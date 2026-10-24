package br.fatec.les.livraria.catalogo.dominio.calculo;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Calcula o preço de venda do livro como maior custo entre as entradas de estoque × (1 + margem do grupo de
 * precificação do livro) (RF-EST-002, RN-EST-002, RN-LIV-003).
 *
 * <p>A {@code margem} é uma fração: {@code 0.2000} representa 20%. O cálculo fica em {@link BigDecimal} sem
 * arredondamentos intermediários; só o resultado vira {@link Dinheiro}, com 2 casas e {@code HALF_UP}
 * (DEC-15.1). Exemplo: custo {@code 33.33} com margem de 15% ({@code 0.15}) → {@code 38.3295} → R$ 38,33.
 */
@Component
public class CalcularPrecoVenda {

    /**
     * @param maiorCusto maior custo entre as entradas de estoque do livro
     * @param margem     margem de lucro do grupo de precificação, como fração
     * @return o preço de venda arredondado a 2 casas com {@code HALF_UP}
     */
    public Dinheiro calcular(BigDecimal maiorCusto, BigDecimal margem) {
        Objects.requireNonNull(maiorCusto, "maiorCusto");
        Objects.requireNonNull(margem, "margem");

        BigDecimal fator = BigDecimal.ONE.add(margem);
        return Dinheiro.de(maiorCusto.multiply(fator));
    }
}
