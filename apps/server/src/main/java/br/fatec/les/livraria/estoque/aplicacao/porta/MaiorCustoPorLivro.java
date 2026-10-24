package br.fatec.les.livraria.estoque.aplicacao.porta;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * Porta de saída do maior custo por livro entre as entradas de estoque (RN-EST-002, DEC-15.3).
 *
 * <p>A implementação consulta todas as entradas dos livros informados em uma única agregação.
 */
public interface MaiorCustoPorLivro {

    /**
     * @param livroIds livros a consultar
     * @return maior custo por id de livro; livro sem entrada não aparece no mapa
     */
    Map<Long, BigDecimal> buscar(Collection<Long> livroIds);
}
