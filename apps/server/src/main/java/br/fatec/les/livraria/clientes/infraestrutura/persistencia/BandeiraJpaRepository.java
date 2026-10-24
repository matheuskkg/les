package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

/** Bandeiras do seed do CLI-01 (RN-CLI-005, DEC-16). */
public interface BandeiraJpaRepository extends JpaRepository<BandeiraJpa, Long> {
}
