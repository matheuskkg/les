package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExemploListagemRepositorio
        extends JpaRepository<ExemploListagemJpa, Long>, JpaSpecificationExecutor<ExemploListagemJpa> {
}
