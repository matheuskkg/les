package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

/** Log das tentativas de chamada à IA (RNF-REC-001, DEC-03); só recebe inserções. */
public interface ChamadaIaJpaRepository extends JpaRepository<ChamadaIaJpa, Long> {}
