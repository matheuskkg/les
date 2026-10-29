package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Livro como as tools de busca e o catálogo compacto do chat o devolvem ao assistente (RNF-REC-001, DEC-03). Só
 * livros ativos e com estoque chegam aqui.
 */
public record ResumoDeLivro(
        Long id,
        String titulo,
        List<String> autores,
        List<String> categorias,
        Dinheiro preco) {

    public ResumoDeLivro {
        autores = List.copyOf(autores);
        categorias = List.copyOf(categorias);
    }

    /** Converte um item da vitrine, que já tem preço e disponível calculados (DEC-15.3). */
    public static ResumoDeLivro de(LivroListado livro) {
        return new ResumoDeLivro(livro.livro().id(), livro.livro().titulo(), livro.livro().autores(),
                livro.livro().categorias().stream().map(CategoriaDoLivro::nome).toList(),
                Objects.requireNonNull(livro.precoVenda(), "precoVenda"));
    }
}
