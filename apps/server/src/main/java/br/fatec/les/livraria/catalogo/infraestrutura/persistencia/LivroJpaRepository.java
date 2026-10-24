package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LivroJpaRepository extends JpaRepository<LivroJpa, Long>, JpaSpecificationExecutor<LivroJpa> {

    /** Margem do grupo de precificação de cada livro, em uma única consulta (RN-LIV-003). */
    @Query("""
            select l.id as livroId, l.grupoPrecificacao.margem as margem
            from LivroJpa l
            where l.id in :livroIds
            """)
    List<MargemDoLivroProjecao> buscarMargensPorLivro(@Param("livroIds") Collection<Long> livroIds);

    /**
     * Livro ativo pelo código; a vitrine nunca expõe livro inativo (DEC-07). A editora vem na mesma consulta
     * para não gerar uma consulta por item (RF-LIV-005).
     */
    @EntityGraph(attributePaths = "editora")
    Optional<LivroJpa> findByIdAndAtivoTrue(Long id);

    /**
     * Página da vitrine com a editora carregada junto (RF-LIV-005). A contagem continua sendo gerada pelo
     * Spring Data a partir da mesma {@link Specification}, sem os carregamentos antecipados.
     */
    @EntityGraph(attributePaths = "editora")
    @Override
    Page<LivroJpa> findAll(Specification<LivroJpa> especificacao, Pageable paginacao);

    /** Autores dos livros da página em uma única consulta, sem N+1 (RF-LIV-005). */
    @Query("""
            select l.id as livroId, a.nome as nome
            from LivroJpa l join l.autores a
            where l.id in :livroIds
            order by l.id, a.nome
            """)
    List<AutorDoLivroProjecao> buscarAutoresPorLivro(@Param("livroIds") Collection<Long> livroIds);

    /** Categorias dos livros da página em uma única consulta, sem N+1 (RF-LIV-005, RN-LIV-002). */
    @Query("""
            select l.id as livroId, c.id as categoriaId, c.nome as nome
            from LivroJpa l join l.categorias c
            where l.id in :livroIds
            order by l.id, c.nome
            """)
    List<CategoriaDoLivroProjecao> buscarCategoriasPorLivro(@Param("livroIds") Collection<Long> livroIds);
}
