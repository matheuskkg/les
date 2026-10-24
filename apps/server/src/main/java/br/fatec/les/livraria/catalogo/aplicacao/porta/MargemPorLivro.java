package br.fatec.les.livraria.catalogo.aplicacao.porta;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * Porta de saída para a margem do grupo de precificação de cada livro (RN-LIV-003, RN-EST-002).
 *
 * <p>A implementação consulta todos os livros informados em uma única consulta.
 */
public interface MargemPorLivro {

    /**
     * @param livroIds livros a consultar
     * @return margem do grupo de precificação por id de livro, como fração; livro inexistente não aparece no mapa
     */
    Map<Long, BigDecimal> buscar(Collection<Long> livroIds);
}
