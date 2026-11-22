package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CupomJpaRepository extends JpaRepository<CupomJpa, Long> {

    List<CupomJpa> findByClienteIdAndIdIn(Long clienteId, Collection<Long> ids);

    List<CupomJpa> findByClienteIdAndSituacaoOrderByGeradoEmAscIdAsc(Long clienteId, SituacaoCupom situacao);
}
