package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.EnriquecerComPrecoEDisponibilidade;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import lombok.RequiredArgsConstructor;

/**
 * Lista a vitrine de livros ativos (RF-LIV-005). Consulta o catálogo com o filtro combinado e delega o
 * enriquecimento com preço e disponível a {@link EnriquecerComPrecoEDisponibilidade}.
 */
@Component
@RequiredArgsConstructor
public class ListarLivros {

    private final CatalogoConsultas catalogoConsultas;
    private final EnriquecerComPrecoEDisponibilidade enriquecerComPrecoEDisponibilidade;

    /**
     * @param filtro    campos combinados com E; não pode ser nulo
     * @param paginacao página, tamanho e ordenação já validados; não pode ser nula
     * @return a página de livros ativos com preço e disponível
     */
    @Transactional(readOnly = true)
    public Pagina<LivroListado> executar(FiltroLivro filtro, Paginacao paginacao) {
        Objects.requireNonNull(filtro, "filtro");
        Objects.requireNonNull(paginacao, "paginacao");

        Pagina<LivroResumo> pagina = catalogoConsultas.listar(filtro, paginacao);
        return enriquecerComPrecoEDisponibilidade.enriquecer(pagina);
    }
}
