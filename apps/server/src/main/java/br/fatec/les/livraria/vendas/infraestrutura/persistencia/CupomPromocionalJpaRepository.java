package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CupomPromocionalJpaRepository extends JpaRepository<CupomPromocionalJpa, Long> {

    Optional<CupomPromocionalJpa> findByCodigo(String codigo);
}
