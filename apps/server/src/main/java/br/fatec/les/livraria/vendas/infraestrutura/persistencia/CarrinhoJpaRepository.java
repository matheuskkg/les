package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CarrinhoJpaRepository extends JpaRepository<CarrinhoJpa, Long> {

    /** Carrinho do cliente com os itens, numa consulta só. */
    @EntityGraph(attributePaths = "itens")
    Optional<CarrinhoJpa> findByClienteId(Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CarrinhoJpa c where c.clienteId = :clienteId")
    Optional<CarrinhoJpa> buscarPorClienteComLock(@Param("clienteId") Long clienteId);
}
