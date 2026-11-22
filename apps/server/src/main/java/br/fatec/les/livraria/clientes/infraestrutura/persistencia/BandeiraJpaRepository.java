package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Bandeiras do seed do CLI-01 (RN-CLI-005, DEC-16, DEC-18). */
public interface BandeiraJpaRepository extends JpaRepository<BandeiraJpa, Long> {

    List<BandeiraJpa> findAllByOrderByIdAsc();
}
