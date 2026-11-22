package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversaJpaRepository extends JpaRepository<ConversaJpa, Long> {

    Optional<ConversaJpa> findFirstByClienteIdAndArquivadaEmIsNullOrderByIdDesc(Long clienteId);
}
