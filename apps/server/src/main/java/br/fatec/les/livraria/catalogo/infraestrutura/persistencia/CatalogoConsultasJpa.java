package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ConversorDePaginacao;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA das consultas de leitura do catálogo (RF-LIV-005, RNF-LIV-001, DEC-07).
 *
 * <p>Autores, categorias e editora dos livros de uma página são carregados em três consultas em lote, para
 * não gerar uma consulta por item. A consulta paginada em si usa as {@link EspecificacoesDeLivro}, mantendo
 * a contagem correta mesmo com os filtros por relacionamento.
 */
@Component
@RequiredArgsConstructor
public class CatalogoConsultasJpa implements CatalogoConsultas {

    private final LivroJpaRepository livros;
    private final CategoriaJpaRepository categorias;

    @Override
    public Pagina<LivroResumo> listar(FiltroLivro filtro, Paginacao paginacao) {
        Page<LivroJpa> pagina = livros.findAll(EspecificacoesDeLivro.de(filtro),
                ConversorDePaginacao.paraPageable(paginacao));
        Relacionamentos relacionamentos = carregar(pagina.getContent().stream()
                .map(LivroJpa::getId)
                .toList());
        return ConversorDePaginacao.paraPagina(pagina, livro -> paraResumo(livro, relacionamentos));
    }

    @Override
    public Optional<LivroDetalhe> buscar(Long id) {
        return livros.findByIdAndAtivoTrue(id)
                .map(livro -> paraDetalhe(livro, carregar(List.of(livro.getId()))));
    }

    @Override
    public List<CategoriaDoLivro> listarCategorias() {
        return categorias.findAllByOrderByNomeAsc().stream()
                .map(categoria -> new CategoriaDoLivro(categoria.getId(), categoria.getNome()))
                .toList();
    }

    /** Carrega os relacionamentos dos livros informados em lote. */
    private Relacionamentos carregar(List<Long> livroIds) {
        if (livroIds.isEmpty()) {
            return Relacionamentos.vazios();
        }

        Map<Long, List<String>> autores = new LinkedHashMap<>();
        for (AutorDoLivroProjecao autor : livros.buscarAutoresPorLivro(livroIds)) {
            autores.computeIfAbsent(autor.getLivroId(), id -> new ArrayList<>()).add(autor.getNome());
        }

        Map<Long, List<CategoriaDoLivro>> categoriasPorLivro = new LinkedHashMap<>();
        for (CategoriaDoLivroProjecao categoria : livros.buscarCategoriasPorLivro(livroIds)) {
            categoriasPorLivro.computeIfAbsent(categoria.getLivroId(), id -> new ArrayList<>())
                    .add(new CategoriaDoLivro(categoria.getCategoriaId(), categoria.getNome()));
        }

        Map<Long, String> editoras = livros.buscarEditorasPorLivro(livroIds).stream()
                .collect(Collectors.toMap(
                        EditoraDoLivroProjecao::getLivroId,
                        EditoraDoLivroProjecao::getNome));

        return new Relacionamentos(congelar(autores), congelar(categoriasPorLivro), Map.copyOf(editoras));
    }

    private static LivroResumo paraResumo(LivroJpa livro, Relacionamentos relacionamentos) {
        Long id = livro.getId();
        return new LivroResumo(id, livro.getTitulo(),
                relacionamentos.autores().getOrDefault(id, List.of()),
                relacionamentos.categorias().getOrDefault(id, List.of()),
                relacionamentos.editoras().get(id),
                livro.getAno());
    }

    private static LivroDetalhe paraDetalhe(LivroJpa livro, Relacionamentos relacionamentos) {
        LivroResumo resumo = paraResumo(livro, relacionamentos);
        return new LivroDetalhe(resumo.id(), resumo.titulo(), resumo.autores(), resumo.categorias(),
                resumo.editora(), resumo.ano(), livro.getEdicao(), livro.getIsbn(), livro.getPaginas(),
                livro.getSinopse(), livro.getAltura(), livro.getLargura(), livro.getProfundidade(),
                livro.getPeso(), livro.getCodigoBarras());
    }

    private static <T> Map<Long, List<T>> congelar(Map<Long, List<T>> mapa) {
        Map<Long, List<T>> copia = new LinkedHashMap<>();
        mapa.forEach((chave, valores) -> copia.put(chave, List.copyOf(valores)));
        return Map.copyOf(copia);
    }

    /** Relacionamentos de uma página, indexados pelo id do livro. */
    private record Relacionamentos(
            Map<Long, List<String>> autores,
            Map<Long, List<CategoriaDoLivro>> categorias,
            Map<Long, String> editoras) {

        private static Relacionamentos vazios() {
            return new Relacionamentos(Map.of(), Map.of(), Map.of());
        }
    }
}
