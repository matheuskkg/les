package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;

public interface CupomJpaRepository extends JpaRepository<CupomJpa, Long> {

    List<CupomJpa> findByClienteIdAndIdIn(Long clienteId, Collection<Long> ids);

    List<CupomJpa> findByClienteIdAndSituacaoOrderByGeradoEmAscIdAsc(Long clienteId, SituacaoCupom situacao);
}
