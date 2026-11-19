package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.Especificacoes;

/**
 * Traduz o {@link FiltroLivro} para as {@link Specification} do catálogo (RF-LIV-005, DEC-07). Os campos se
 * combinam com E; autor e categoria usam subconsulta {@code exists}, e a vitrine só enxerga livro ativo. A busca
 * livre ({@code q}) casa título, autor, editora ou ISBN, com OU entre eles.
 */
final class EspecificacoesDeLivro {

    private EspecificacoesDeLivro() {
    }

    static Specification<LivroJpa> de(FiltroLivro filtro) {
        Objects.requireNonNull(filtro, "filtro");
        return Specification.<LivroJpa>unrestricted()
                .and(Especificacoes.igual("ativo", Boolean.TRUE))
                .and(Especificacoes.contem("titulo", filtro.titulo()))
                .and(Especificacoes.existeContem("autores", "nome", filtro.autor()))
                .and(Especificacoes.existe("categorias", "id", filtro.categoria()))
                .and(Especificacoes.contem("editora", "nome", filtro.editora()))
                .and(Especificacoes.digitos("isbn", filtro.isbn()))
                .and(Especificacoes.igual("codigoBarras", filtro.codigoBarras()))
                .and(Especificacoes.entre("ano", filtro.anoDe(), filtro.anoAte()))
                .and(Especificacoes.contem("edicao", filtro.edicao()))
                .and(Especificacoes.entre("paginas", filtro.paginasDe(), filtro.paginasAte()))
                .and(Especificacoes.igual("id", filtro.codigo()))
                .and(buscaLivre(filtro.q()));
    }

    private static Specification<LivroJpa> buscaLivre(String q) {
        if (q == null || q.isBlank()) {
            return Specification.unrestricted();
        }
        return Specification.anyOf(
                Especificacoes.contem("titulo", q),
                Especificacoes.existeContem("autores", "nome", q),
                Especificacoes.contem("editora", "nome", q),
                Especificacoes.contem("isbn", q));
    }
}
