package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrocaJpaRepository extends JpaRepository<TrocaJpa, Long> {

    List<TrocaJpa> findByPedidoId(Long pedidoId);
}
