package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorJpaRepository extends JpaRepository<AutorJpa, Long> {
}
