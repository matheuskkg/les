package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.porta.MargemPorLivro;
import br.fatec.les.livraria.catalogo.dominio.calculo.CalcularPrecoVenda;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarMaiorCusto;
import lombok.RequiredArgsConstructor;

/**
 * Calcula o preço de venda de vários livros de uma vez (RF-EST-002, RN-EST-002, RN-LIV-003).
 *
 * <p>O preço não é gravado: usa a margem do grupo de precificação do livro e o maior custo entre as
 * entradas de estoque, ambos lidos em lote e agregados por {@link CalcularPrecoVenda}. Um id ausente do
 * mapa devolvido significa que o livro não tem entrada de estoque ou não existe.
 */
@Component
@RequiredArgsConstructor
public class ConsultarPrecosDeVenda {

    private final MargemPorLivro margemPorLivro;
    private final ConsultarMaiorCusto consultarMaiorCusto;
    private final CalcularPrecoVenda calcularPrecoVenda;

    /**
     * Calcula o preço de venda em lote. A ordem das chaves do mapa não faz parte do contrato.
     *
     * @param livroIds livros a consultar; não pode ser nulo nem conter elementos nulos
     * @return preço por id de livro; livro sem entrada de estoque ou inexistente não aparece no mapa
     */
    @Transactional(readOnly = true)
    public Map<Long, Dinheiro> executar(Collection<Long> livroIds) {
        Objects.requireNonNull(livroIds, "livroIds");
        livroIds.forEach(livroId -> Objects.requireNonNull(livroId, "livroIds não pode conter null"));
        if (livroIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, BigDecimal> margens = margemPorLivro.buscar(livroIds);
        Map<Long, BigDecimal> maioresCustos = consultarMaiorCusto.executar(livroIds);

        Map<Long, Dinheiro> precos = new HashMap<>();
        for (Long livroId : livroIds) {
            BigDecimal custo = maioresCustos.get(livroId);
            BigDecimal margem = margens.get(livroId);
            if (custo != null && margem != null) {
                precos.put(livroId, calcularPrecoVenda.calcular(custo, margem));
            }
        }
        return Map.copyOf(precos);
    }
}
