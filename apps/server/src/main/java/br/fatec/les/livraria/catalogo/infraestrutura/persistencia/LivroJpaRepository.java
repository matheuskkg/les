package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LivroJpaRepository extends JpaRepository<LivroJpa, Long> {

    /** Margem do grupo de precificação de cada livro, em uma única consulta (RN-LIV-003). */
    @Query("""
            select l.id as livroId, l.grupoPrecificacao.margem as margem
            from LivroJpa l
            where l.id in :livroIds
            """)
    List<MargemDoLivroProjecao> buscarMargensPorLivro(@Param("livroIds") Collection<Long> livroIds);
}
