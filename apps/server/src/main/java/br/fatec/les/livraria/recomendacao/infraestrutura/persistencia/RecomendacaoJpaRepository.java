package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecomendacaoJpaRepository extends JpaRepository<RecomendacaoJpa, Long> {

    @EntityGraph(attributePaths = "itens")
    Optional<RecomendacaoJpa> findFirstByClienteIdOrderByGeradaEmDescIdDesc(Long clienteId);

    @EntityGraph(attributePaths = "itens")
    @Query("select i.recomendacao from ItemRecomendacaoJpa i where i.id = :itemId")
    Optional<RecomendacaoJpa> buscarPorItem(@Param("itemId") Long itemId);

    /** Itens avaliados do cliente em todas as recomendações, do mais antigo ao mais recente (DEC-03). */
    @Query(
            "select i from ItemRecomendacaoJpa i where i.recomendacao.clienteId = :clienteId "
                    + "and i.avaliacao is not null order by i.avaliadaEm, i.id")
    List<ItemRecomendacaoJpa> avaliadosDoCliente(@Param("clienteId") Long clienteId);
}
