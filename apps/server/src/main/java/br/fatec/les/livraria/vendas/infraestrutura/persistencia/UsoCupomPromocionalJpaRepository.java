package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsoCupomPromocionalJpaRepository extends JpaRepository<UsoCupomPromocionalJpa, Long> {

    boolean existsByCupomPromocionalIdAndClienteId(Long cupomPromocionalId, Long clienteId);

    void deleteByPedidoId(Long pedidoId);
}
