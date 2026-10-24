package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExemploTagRepositorio extends JpaRepository<ExemploTagJpa, Long> {
}
