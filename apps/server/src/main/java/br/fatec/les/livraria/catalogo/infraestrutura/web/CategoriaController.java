package br.fatec.les.livraria.catalogo.infraestrutura.web;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ListarCategorias;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.CategoriaDto;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Categorias para preencher o filtro da vitrine (RF-LIV-005, RN-LIV-002). É uma listagem paginada como as demais do
 * DEC-07, ordenada por nome por padrão.
 */
@RestController
@RequestMapping("/api/livros/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final ListarCategorias listarCategorias;
    private final MapeadorDeLivros mapeador;

    @GetMapping
    public Pagina<CategoriaDto> listarCategorias(@Listagem(campos = "nome", padrao = "nome,asc") Paginacao paginacao) {
        return listarCategorias.executar(paginacao).map(mapeador::paraDto);
    }
}
