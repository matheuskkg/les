package br.fatec.les.livraria.catalogo.aplicacao.porta;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.MedidasDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.SinopseDoLivro;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saída das consultas de leitura do catálogo (RF-LIV-005, DEC-07). A implementação devolve só livros ativos e
 * carrega autores, categorias e editora em lote, sem uma consulta por item.
 */
public interface CatalogoConsultas {

    /**
     * @param filtro campos combinados com E; categorias repetidas se combinam com OU
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
     * @param paginacao página, tamanho e ordenação já validados
     * @return a página de categorias
     */
    Pagina<CategoriaDoLivro> listarCategorias(Paginacao paginacao);

    /**
     * @param ids ids das categorias desejadas
     * @return as categorias existentes entre as informadas, em qualquer ordem
     */
    List<CategoriaDoLivro> buscarCategorias(Collection<Long> ids);

    /**
     * Livros pelos ids, ativos ou não, com título e autores, para a leitura do carrinho (RF-VEN-001). A situação é
     * mantida para que o {@code vendas} recuse um livro inativo na adição.
     *
     * @param livroIds livros a consultar
     * @return os livros encontrados, em ordem indeterminada; id inexistente não aparece
     */
    List<LivroParaVenda> buscarParaVenda(Collection<Long> livroIds);

    /**
     * Peso e dimensões dos livros pelos ids, ativos ou não, para o cálculo de frete (RF-VEN-004, RN-LIV-001).
     *
     * @param livroIds livros a consultar
     * @return as medidas encontradas, em ordem indeterminada; id inexistente não aparece
     */
    List<MedidasDoLivro> buscarMedidas(Collection<Long> livroIds);

    /**
     * Sinopse dos livros pelos ids, para a busca do assistente (RNF-REC-001, DEC-03).
     *
     * @param livroIds livros a consultar
     * @return as sinopses encontradas, em ordem indeterminada; id inexistente não aparece
     */
    List<SinopseDoLivro> buscarSinopses(Collection<Long> livroIds);

    /**
     * Livros ativos pelos ids, com autores, categorias e editora, para exibir numa conversa só os citados (RNF-REC-001,
     * DEC-03). Um id só em vez da vitrine inteira: quem precisa de poucos livros não lê o catálogo todo (RNF-GER-001).
     *
     * @param livroIds livros a consultar
     * @return os livros ativos encontrados, em ordem indeterminada; inativo ou inexistente não aparece
     */
    List<LivroResumo> buscarAtivos(Collection<Long> livroIds);
}
