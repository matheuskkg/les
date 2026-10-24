package br.fatec.les.livraria.catalogo.infraestrutura.web;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroConsultado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.CategoriaDto;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.LivroDetalheDto;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.LivroListadoDto;

/** Converte os modelos de consulta do catálogo nos DTOs da API (RF-LIV-005, DEC-07). */
@Component
public class MapeadorDeLivros {

    public LivroListadoDto paraDto(LivroListado livro) {
        return new LivroListadoDto(livro.id(), livro.titulo(), livro.autores(), paraDto(livro.categorias()),
                livro.editora(), livro.ano(), livro.precoVenda(), livro.disponivel());
    }

    public LivroDetalheDto paraDto(LivroConsultado livro) {
        return new LivroDetalheDto(livro.id(), livro.titulo(), livro.autores(), paraDto(livro.categorias()),
                livro.editora(), livro.ano(), livro.precoVenda(), livro.disponivel(), livro.edicao(),
                livro.isbn(), livro.paginas(), livro.sinopse(), livro.altura(), livro.largura(),
                livro.profundidade(), livro.peso(), livro.codigoBarras());
    }

    public CategoriaDto paraDto(CategoriaDoLivro categoria) {
        return new CategoriaDto(categoria.id(), categoria.nome());
    }

    private List<CategoriaDto> paraDto(List<CategoriaDoLivro> categorias) {
        return categorias.stream().map(this::paraDto).toList();
    }
}
