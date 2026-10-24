package br.fatec.les.livraria.catalogo.dominio.calculo;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Calcula o preço de venda do livro como maior custo entre as entradas de estoque × (1 + margem do grupo de
 * precificação do livro) (RF-EST-002, RN-EST-002, RN-LIV-003).
 *
 * <p>A {@code margem} é uma fração: {@code 0.2000} representa 20%. O cálculo fica em {@link BigDecimal} sem
 * arredondamentos intermediários; só o resultado vira {@link Dinheiro}, com 2 casas e {@code HALF_UP}
 * (DEC-15.1). Exemplo: custo {@code 33.33} com margem de 15% ({@code 0.15}) → {@code 38.3295} → R$ 38,33.
 *
 * <p>Entradas inválidas são rejeitadas com {@link IllegalArgumentException}: custo nulo ou não positivo e margem
 * nula ou negativa.
 */
@Component
public class CalcularPrecoVenda {

    /**
     * @param maiorCusto maior custo entre as entradas de estoque do livro, maior que zero
     * @param margem     margem de lucro do grupo de precificação, como fração não negativa
     * @return o preço de venda arredondado a 2 casas com {@code HALF_UP}
     * @throws IllegalArgumentException se algum argumento for nulo, se {@code maiorCusto <= 0} ou se
     *                                  {@code margem < 0}
     */
    public Dinheiro calcular(BigDecimal maiorCusto, BigDecimal margem) {
        if (maiorCusto == null) {
            throw new IllegalArgumentException("maiorCusto é obrigatório");
        }
        if (margem == null) {
            throw new IllegalArgumentException("margem é obrigatória");
        }
        if (maiorCusto.signum() <= 0) {
            throw new IllegalArgumentException("maiorCusto deve ser maior que zero");
        }
        if (margem.signum() < 0) {
            throw new IllegalArgumentException("margem não pode ser negativa");
        }

        BigDecimal fator = BigDecimal.ONE.add(margem);
        return Dinheiro.de(maiorCusto.multiply(fator));
    }
}
