package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

/**
 * Converte {@link Paginacao}/{@link Pagina} de e para os tipos do Spring Data, que não saem de
 * {@code infraestrutura} (DEC-07). O {@code id} entra sempre como último critério de desempate.
 */
public final class ConversorDePaginacao {

    private static final String CAMPO_ID = "id";

    private ConversorDePaginacao() {
    }

    public static Pageable paraPageable(Paginacao paginacao) {
        Sort ordenacao = Sort.by(paginacao.ordem().stream().map(ConversorDePaginacao::paraOrdenacao).toList());
        if (ordenacao.getOrderFor(CAMPO_ID) == null) {
            ordenacao = ordenacao.and(Sort.by(Sort.Order.asc(CAMPO_ID)));
        }
        return PageRequest.of(paginacao.pagina(), paginacao.tamanho(), ordenacao);
    }

    private static Sort.Order paraOrdenacao(Ordem ordem) {
        Sort.Direction direcao = ordem.direcao() == Ordem.Direcao.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return new Sort.Order(direcao, ordem.campo());
    }

    public static <E> Pagina<E> paraPagina(Page<E> pagina) {
        return new Pagina<>(List.copyOf(pagina.getContent()), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }

    public static <E, T> Pagina<T> paraPagina(Page<E> pagina, Function<? super E, ? extends T> mapeador) {
        return paraPagina(pagina).map(mapeador);
    }
}
