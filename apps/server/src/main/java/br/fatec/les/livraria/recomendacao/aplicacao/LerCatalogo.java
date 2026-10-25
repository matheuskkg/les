package br.fatec.les.livraria.recomendacao.aplicacao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ListarLivros;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import lombok.RequiredArgsConstructor;

/**
 * Lê a vitrine inteira pelo caso de uso público do {@code catalogo}, com preço e disponível (RNF-REC-001, DEC-03).
 * O catálogo é pequeno (seed), então a recuperação trabalha sobre todos os livros ativos em memória.
 */
@Component
@RequiredArgsConstructor
public class LerCatalogo {

    private static final FiltroLivro SEM_FILTRO = new FiltroLivro(null, null, List.of(), null, null, null, null,
            null, null, null, null, null, null);

    private final ListarLivros listarLivros;

    /** @return todos os livros ativos, com preço e disponível */
    public List<LivroListado> ativos() {
        List<LivroListado> livros = new ArrayList<>();
        int pagina = 0;
        Pagina<LivroListado> atual;
        do {
            atual = listarLivros.executar(SEM_FILTRO, new Paginacao(pagina++, Paginacao.TAMANHO_MAXIMO, List.of()));
            livros.addAll(atual.itens());
        } while (pagina < atual.totalPaginas());
        return livros;
    }
}
