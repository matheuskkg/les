package br.fatec.les.livraria.catalogo.infraestrutura.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivro;
import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ListarLivros;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.LivroDetalheDto;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.LivroListadoDto;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;
import lombok.RequiredArgsConstructor;

/**
 * Vitrine de livros: listagem com filtros combinados e detalhe (RF-LIV-005, RNF-LIV-001, DEC-07). Só
 * livros ativos aparecem; livro inativo ou inexistente responde 404.
 */
@RestController
@RequestMapping("/api/livros")
@RequiredArgsConstructor
public class LivroController {

    private final ListarLivros listarLivros;
    private final ConsultarLivro consultarLivro;
    private final MapeadorDeLivros mapeador;

    @GetMapping
    public Pagina<LivroListadoDto> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) List<Long> categoria,
            @RequestParam(required = false) String editora,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String codigoBarras,
            @RequestParam(required = false) Integer anoDe,
            @RequestParam(required = false) Integer anoAte,
            @RequestParam(required = false) String edicao,
            @RequestParam(required = false) Integer paginasDe,
            @RequestParam(required = false) Integer paginasAte,
            @RequestParam(required = false) Long codigo,
            @RequestParam(required = false) String q,
            @Listagem(campos = { "titulo", "ano", "editora=editora.nome" }, padrao = "titulo,asc") Paginacao paginacao) {
        FiltroLivro filtro = new FiltroLivro(titulo, autor, categoria, editora, isbn, codigoBarras,
                anoDe, anoAte, edicao, paginasDe, paginasAte, codigo, q);
        return listarLivros.executar(filtro, paginacao).map(mapeador::paraDto);
    }

    @GetMapping("/{id}")
    public LivroDetalheDto buscar(@PathVariable Long id) {
        return mapeador.paraDto(consultarLivro.executar(id));
    }
}
