package br.fatec.les.livraria.catalogo.infraestrutura.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ListarCategorias;
import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.CategoriaDto;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import lombok.RequiredArgsConstructor;

/**
 * Categorias para preencher o filtro da vitrine (RF-LIV-005, RN-LIV-002). É uma tabela de domínio pequena;
 * a resposta usa o envelope paginado do DEC-07 com o tamanho máximo.
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final ListarCategorias listarCategorias;
    private final MapeadorDeLivros mapeador;

    @GetMapping
    public Pagina<CategoriaDto> listarCategorias() {
        return listarCategorias.executar().map(mapeador::paraDto);
    }
}
