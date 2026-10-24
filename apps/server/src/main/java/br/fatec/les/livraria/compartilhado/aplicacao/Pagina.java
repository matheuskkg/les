package br.fatec.les.livraria.compartilhado.aplicacao;

import java.util.List;
import java.util.function.Function;

/**
 * Uma página de resultado no envelope da API (DEC-07).
 *
 * @param itens itens da página
 * @param pagina página atual, a partir de 0
 * @param tamanho tamanho da página
 * @param totalItens total de itens de todas as páginas
 * @param totalPaginas total de páginas
 * @param <T> tipo dos itens
 */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {

    public Pagina {
        itens = List.copyOf(itens);
    }

    /** Converte os itens preservando o envelope da paginação. */
    public <R> Pagina<R> map(Function<? super T, ? extends R> mapeador) {
        List<R> convertidos = itens.stream().<R>map(mapeador).toList();
        return new Pagina<>(convertidos, pagina, tamanho, totalItens, totalPaginas);
    }
}
