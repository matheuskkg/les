package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
 * entradas de estoque, ambos lidos em lote e agregados por {@link CalcularPrecoVenda}. Livro sem
 * entrada de estoque não tem preço e por isso não aparece no mapa devolvido.
 */
@Component
@RequiredArgsConstructor
public class ConsultarPrecosDeVenda {

    private final MargemPorLivro margemPorLivro;
    private final ConsultarMaiorCusto consultarMaiorCusto;
    private final CalcularPrecoVenda calcularPrecoVenda;

    /**
     * @param livroIds livros a consultar
     * @return preço por id de livro; livro sem entrada de estoque não aparece no mapa
     */
    @Transactional(readOnly = true)
    public Map<Long, Dinheiro> executar(Collection<Long> livroIds) {
        Objects.requireNonNull(livroIds, "livroIds");
        if (livroIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, BigDecimal> margens = margemPorLivro.buscar(livroIds);
        Map<Long, BigDecimal> maioresCustos = consultarMaiorCusto.executar(livroIds);

        Map<Long, Dinheiro> precos = new LinkedHashMap<>();
        for (Long livroId : livroIds) {
            BigDecimal custo = maioresCustos.get(livroId);
            BigDecimal margem = margens.get(livroId);
            if (custo != null && margem != null) {
                precos.put(livroId, calcularPrecoVenda.calcular(custo, margem));
            }
        }
        return Map.copyOf(precos);
    }

    /**
     * Conveniência para a consulta de um único livro.
     *
     * @return o preço de venda, ou vazio se o livro não tiver entrada de estoque ou não existir
     */
    @Transactional(readOnly = true)
    public Optional<Dinheiro> executar(Long livroId) {
        Objects.requireNonNull(livroId, "livroId");
        return Optional.ofNullable(executar(List.of(livroId)).get(livroId));
    }
}
