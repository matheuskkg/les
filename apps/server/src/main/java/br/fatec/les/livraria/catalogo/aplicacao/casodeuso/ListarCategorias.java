package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import lombok.RequiredArgsConstructor;

/**
 * Lista as categorias usadas no filtro da vitrine (RF-LIV-005). É uma tabela de domínio pequena: a resposta
 * usa o envelope de paginação do DEC-07 com o tamanho máximo, porque cabe em uma página só.
 */
@Component
@RequiredArgsConstructor
public class ListarCategorias {

    private final CatalogoConsultas catalogoConsultas;

    @Transactional(readOnly = true)
    public Pagina<CategoriaDoLivro> executar() {
        List<CategoriaDoLivro> itens = catalogoConsultas.listarCategorias();
        int totalPaginas = itens.isEmpty() ? 0 : 1;
        return new Pagina<>(itens, Paginacao.PAGINA_PADRAO, Paginacao.TAMANHO_MAXIMO, itens.size(), totalPaginas);
    }
}
