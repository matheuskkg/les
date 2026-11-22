package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Provedores da cadeia de IA, lidos na ordem de preferência (RNF-REC-001, DEC-03). */
public interface ProvedorIaJpaRepository extends JpaRepository<ProvedorIaJpa, Long> {

    List<ProvedorIaJpa> findAllByOrderByOrdemAscIdAsc();
}
