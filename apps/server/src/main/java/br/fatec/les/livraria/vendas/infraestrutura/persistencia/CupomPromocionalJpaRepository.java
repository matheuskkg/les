package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CupomPromocionalJpaRepository extends JpaRepository<CupomPromocionalJpa, Long> {

    Optional<CupomPromocionalJpa> findByCodigo(String codigo);
}
