package br.fatec.les.livraria.vendas.aplicacao.porta;

import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;

import java.util.Collection;
import java.util.List;

/** Cupons de troca do cliente (RF-VEN-007, DEC-11). */
public interface CuponsDeTroca {

    /**
     * @return os cupons do cliente entre os ids; id de outro cliente ou inexistente não aparece
     */
    List<CupomTroca> buscarDoCliente(Long clienteId, Collection<Long> ids);

    /**
     * @return os cupons não usados do cliente, dos mais antigos aos mais novos, vencidos ou não
     */
    List<CupomTroca> listarNaoUsados(Long clienteId);

    /**
     * Marca os cupons como usados na finalização (DEC-11), conferindo a versão lida na mesma transação.
     *
     * @throws org.springframework.dao.OptimisticLockingFailureException se outro pedido usou um deles antes (409)
     */
    void marcarComoUsados(Collection<Long> ids);

    /** Devolve ao cliente os cupons de um pedido reprovado (DEC-11, DEC-16). */
    void devolver(Collection<Long> ids);

    /** Grava um cupom novo, como o de troco (RN-VEN-006), e o devolve com o id. */
    CupomTroca gerar(CupomTroca cupom);
}
