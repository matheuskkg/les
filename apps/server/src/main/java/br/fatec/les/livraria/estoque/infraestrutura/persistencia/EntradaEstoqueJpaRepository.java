package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EntradaEstoqueJpaRepository extends JpaRepository<EntradaEstoqueJpa, Long> {

    List<EntradaEstoqueJpa> findByLivroId(Long livroId);
}
