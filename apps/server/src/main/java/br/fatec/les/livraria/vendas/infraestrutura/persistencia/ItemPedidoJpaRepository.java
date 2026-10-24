package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoJpaRepository extends JpaRepository<ItemPedidoJpa, Long> {
}
