package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntradaEstoqueJpaRepository extends JpaRepository<EntradaEstoqueJpa, Long> {

    List<EntradaEstoqueJpa> findByLivroId(Long livroId);

    /**
     * Maior custo por livro em uma única agregação (RN-EST-002). Livro sem entrada não aparece no resultado.
     */
    @Query("""
            select e.livroId as livroId, max(e.custo) as maiorCusto
            from EntradaEstoqueJpa e
            where e.livroId in :livroIds
            group by e.livroId
            """)
    List<MaiorCustoDoLivroProjecao> buscarMaiorCustoPorLivro(@Param("livroIds") Collection<Long> livroIds);
}
