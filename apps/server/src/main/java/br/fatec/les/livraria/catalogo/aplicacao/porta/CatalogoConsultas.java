package br.fatec.les.livraria.catalogo.aplicacao.porta;

import java.util.List;
import java.util.Optional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

/**
 * Porta de saída das consultas de leitura do catálogo (RF-LIV-005, DEC-07). A implementação devolve só
 * livros ativos e carrega autores, categorias e editora em lote, sem uma consulta por item.
 */
public interface CatalogoConsultas {

    /**
     * @param filtro    campos combinados com E; categorias repetidas se combinam com OU
     * @param paginacao página, tamanho e ordenação já validados
     * @return a página de livros ativos que atende ao filtro
     */
    Pagina<LivroResumo> listar(FiltroLivro filtro, Paginacao paginacao);

    /**
     * @param id código do livro
     * @return o livro ativo, ou vazio se não existir ou estiver inativo
     */
    Optional<LivroDetalhe> buscar(Long id);

    /**
     * @return todas as categorias, ordenadas por nome
     */
    List<CategoriaDoLivro> listarCategorias();
}
