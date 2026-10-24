package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import lombok.RequiredArgsConstructor;

/**
 * Lista as categorias usadas no filtro da vitrine (RF-LIV-005, RN-LIV-002), com paginação real: o envelope
 * reflete a página pedida e o total de categorias, como em qualquer listagem do DEC-07.
 */
@Component
@RequiredArgsConstructor
public class ListarCategorias {

    private final CatalogoConsultas catalogoConsultas;

    /**
     * @param paginacao página, tamanho e ordenação já validados; não pode ser nula
     * @return a página de categorias
     */
    @Transactional(readOnly = true)
    public Pagina<CategoriaDoLivro> executar(Paginacao paginacao) {
        Objects.requireNonNull(paginacao, "paginacao");
        return catalogoConsultas.listarCategorias(paginacao);
    }
}
