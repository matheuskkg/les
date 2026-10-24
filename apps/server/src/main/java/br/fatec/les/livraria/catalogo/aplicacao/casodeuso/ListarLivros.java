package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarDisponibilidade;
import lombok.RequiredArgsConstructor;

/**
 * Lista a vitrine de livros ativos (RF-LIV-005). Consulta o catálogo com o filtro combinado e enriquece a
 * página com o preço de venda (RF-EST-002, RN-EST-002) e o disponível (DEC-15.3) em lote, sem uma consulta
 * por item. Livro sem preço aparece sem {@code precoVenda} e com disponível zero.
 */
@Component
@RequiredArgsConstructor
public class ListarLivros {

    private final CatalogoConsultas catalogoConsultas;
    private final ConsultarPrecosDeVenda consultarPrecosDeVenda;
    private final ConsultarDisponibilidade consultarDisponibilidade;

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
        List<Long> livroIds = pagina.itens().stream().map(LivroResumo::id).toList();
        if (livroIds.isEmpty()) {
            return pagina.map(livro -> enriquecer(livro, Map.of(), Map.of()));
        }

        Map<Long, Dinheiro> precos = consultarPrecosDeVenda.executar(livroIds);
        Map<Long, Integer> disponiveis = consultarDisponibilidade.executar(livroIds);
        return pagina.map(livro -> enriquecer(livro, precos, disponiveis));
    }

    private static LivroListado enriquecer(LivroResumo livro, Map<Long, Dinheiro> precos,
            Map<Long, Integer> disponiveis) {
        Dinheiro preco = precos.get(livro.id());
        int disponivel = preco == null ? 0 : disponiveis.getOrDefault(livro.id(), 0);
        return new LivroListado(livro.id(), livro.titulo(), livro.autores(), livro.categorias(),
                livro.editora(), livro.ano(), preco, disponivel);
    }
}
