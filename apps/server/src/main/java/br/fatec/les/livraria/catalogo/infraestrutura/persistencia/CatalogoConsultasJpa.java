package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.MedidasDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.SinopseDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ConversorDePaginacao;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter JPA das consultas de leitura do catálogo (RF-LIV-005, RNF-LIV-001, DEC-07).
 *
 * <p>A editora vem na própria consulta paginada, por {@code @EntityGraph}. Autores e categorias dos livros de uma
 * página são carregados em duas consultas em lote, para não gerar uma consulta por item. A consulta paginada em si usa
 * as {@link EspecificacoesDeLivro}, mantendo a contagem correta mesmo com os filtros por relacionamento.
 */
@Component
@RequiredArgsConstructor
public class CatalogoConsultasJpa implements CatalogoConsultas {

    private final LivroJpaRepository livros;
    private final CategoriaJpaRepository categorias;

    @Override
    public Pagina<LivroResumo> listar(FiltroLivro filtro, Paginacao paginacao) {
        Page<LivroJpa> pagina =
                livros.findAll(EspecificacoesDeLivro.de(filtro), ConversorDePaginacao.paraPageable(paginacao));
        Relacionamentos relacionamentos = carregar(pagina.getContent().stream().map(LivroJpa::getId).toList());
        return ConversorDePaginacao.paraPagina(pagina, livro -> paraResumo(livro, relacionamentos));
    }

    @Override
    public Optional<LivroDetalhe> buscar(Long id) {
        return livros.findByIdAndAtivoTrue(id).map(livro -> paraDetalhe(livro, carregar(List.of(livro.getId()))));
    }

    @Override
    public Pagina<CategoriaDoLivro> listarCategorias(Paginacao paginacao) {
        Page<CategoriaJpa> pagina = categorias.findAll(ConversorDePaginacao.paraPageable(paginacao));
        return ConversorDePaginacao.paraPagina(
                pagina, categoria -> new CategoriaDoLivro(categoria.getId(), categoria.getNome()));
    }

    @Override
    public List<CategoriaDoLivro> buscarCategorias(Collection<Long> ids) {
        return ids.isEmpty()
                ? List.of()
                : categorias.findAllById(ids).stream()
                        .map(categoria -> new CategoriaDoLivro(categoria.getId(), categoria.getNome()))
                        .toList();
    }

    @Override
    public List<LivroParaVenda> buscarParaVenda(Collection<Long> livroIds) {
        if (livroIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<String>> autores = new LinkedHashMap<>();
        for (AutorDoLivroProjecao autor : livros.buscarAutoresPorLivro(livroIds)) {
            autores.computeIfAbsent(autor.getLivroId(), id -> new ArrayList<>()).add(autor.getNome());
        }

        return livros.buscarParaVenda(livroIds).stream()
                .map(livro ->
                        new LivroParaVenda(
                                livro.getLivroId(),
                                livro.getTitulo(),
                                autores.getOrDefault(livro.getLivroId(), List.of()),
                                livro.isAtivo()))
                .toList();
    }

    @Override
    public List<MedidasDoLivro> buscarMedidas(Collection<Long> livroIds) {
        if (livroIds.isEmpty()) {
            return List.of();
        }
        return livros.buscarMedidas(livroIds).stream()
                .map(livro ->
                        new MedidasDoLivro(
                                livro.getLivroId(),
                                livro.getPeso(),
                                livro.getAltura(),
                                livro.getLargura(),
                                livro.getProfundidade()))
                .toList();
    }

    @Override
    public List<SinopseDoLivro> buscarSinopses(Collection<Long> livroIds) {
        if (livroIds.isEmpty()) {
            return List.of();
        }
        return livros.buscarSinopses(livroIds).stream()
                .map(livro -> new SinopseDoLivro(livro.getLivroId(), livro.getSinopse()))
                .toList();
    }

    @Override
    public List<LivroResumo> buscarAtivos(Collection<Long> livroIds) {
        if (livroIds.isEmpty()) {
            return List.of();
        }
        List<LivroJpa> ativos = livros.findByIdInAndAtivoTrue(livroIds);
        Relacionamentos relacionamentos = carregar(ativos.stream().map(LivroJpa::getId).toList());
        return ativos.stream().map(livro -> paraResumo(livro, relacionamentos)).toList();
    }

    /** Carrega autores e categorias dos livros informados em lote. */
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
            categoriasPorLivro
                    .computeIfAbsent(categoria.getLivroId(), id -> new ArrayList<>())
                    .add(new CategoriaDoLivro(categoria.getCategoriaId(), categoria.getNome()));
        }

        return new Relacionamentos(congelar(autores), congelar(categoriasPorLivro));
    }

    private static LivroResumo paraResumo(LivroJpa livro, Relacionamentos relacionamentos) {
        Long id = livro.getId();
        return new LivroResumo(
                id,
                livro.getTitulo(),
                relacionamentos.autores().getOrDefault(id, List.of()),
                relacionamentos.categorias().getOrDefault(id, List.of()),
                livro.getEditora().getNome(),
                livro.getAno());
    }

    private static LivroDetalhe paraDetalhe(LivroJpa livro, Relacionamentos relacionamentos) {
        return new LivroDetalhe(
                paraResumo(livro, relacionamentos),
                livro.getEdicao(),
                livro.getIsbn(),
                livro.getPaginas(),
                livro.getSinopse(),
                livro.getAltura(),
                livro.getLargura(),
                livro.getProfundidade(),
                livro.getPeso(),
                livro.getCodigoBarras());
    }

    private static <T> Map<Long, List<T>> congelar(Map<Long, List<T>> mapa) {
        Map<Long, List<T>> copia = new LinkedHashMap<>();
        mapa.forEach((chave, valores) -> copia.put(chave, List.copyOf(valores)));
        return Map.copyOf(copia);
    }

    /** Relacionamentos de uma página, indexados pelo id do livro. */
    private record Relacionamentos(Map<Long, List<String>> autores, Map<Long, List<CategoriaDoLivro>> categorias) {

        private static Relacionamentos vazios() {
            return new Relacionamentos(Map.of(), Map.of());
        }
    }
}
