package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversaJpaRepository extends JpaRepository<ConversaJpa, Long> {

    Optional<ConversaJpa> findFirstByClienteIdAndArquivadaEmIsNullOrderByIdDesc(Long clienteId);
}
