package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaJpaRepository extends JpaRepository<ReservaJpa, Long> {
}
