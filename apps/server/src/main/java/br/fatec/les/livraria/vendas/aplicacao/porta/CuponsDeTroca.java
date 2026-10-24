package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.util.Collection;
import java.util.List;

import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;

/** Cupons de troca do cliente (RF-VEN-007, DEC-11). */
public interface CuponsDeTroca {

    /** @return os cupons do cliente entre os ids; id de outro cliente ou inexistente não aparece */
    List<CupomTroca> buscarDoCliente(Long clienteId, Collection<Long> ids);

    /** @return os cupons não usados do cliente, dos mais antigos aos mais novos, vencidos ou não */
    List<CupomTroca> listarNaoUsados(Long clienteId);
}
