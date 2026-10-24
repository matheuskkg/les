package br.fatec.les.livraria.catalogo.infraestrutura.web;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroConsultado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.CategoriaDto;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.LivroDetalheDto;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.LivroListadoDto;

/** Converte os modelos de consulta do catálogo nos DTOs da API (RF-LIV-005, DEC-07). */
@Component
public class MapeadorDeLivros {

    public LivroListadoDto paraDto(LivroListado livro) {
        LivroResumo resumo = livro.livro();
        return new LivroListadoDto(resumo.id(), resumo.titulo(), resumo.autores(),
                paraDto(resumo.categorias()), resumo.editora(), resumo.ano(), livro.precoVenda(),
                livro.disponivel());
    }

    public LivroDetalheDto paraDto(LivroConsultado consultado) {
        LivroDetalhe detalhe = consultado.livro();
        LivroResumo resumo = detalhe.livro();
        return new LivroDetalheDto(resumo.id(), resumo.titulo(), resumo.autores(),
                paraDto(resumo.categorias()), resumo.editora(), resumo.ano(), consultado.precoVenda(),
                consultado.disponivel(), detalhe.edicao(), detalhe.isbn(), detalhe.paginas(),
                detalhe.sinopse(), detalhe.altura(), detalhe.largura(), detalhe.profundidade(),
                detalhe.peso(), detalhe.codigoBarras());
    }

    public CategoriaDto paraDto(CategoriaDoLivro categoria) {
        return new CategoriaDto(categoria.id(), categoria.nome());
    }

    private List<CategoriaDto> paraDto(List<CategoriaDoLivro> categorias) {
        return categorias.stream().map(this::paraDto).toList();
    }
}
