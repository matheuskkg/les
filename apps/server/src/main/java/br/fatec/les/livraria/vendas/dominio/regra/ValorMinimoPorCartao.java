package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.CartoesDaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.PagamentoEmCartao;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Valor mínimo de R$ 10,00 por cartão (RN-VEN-004). Exceção (RN-VEN-005, DEC-11): havendo cupom e restante menor que R$
 * 10,00, um único cartão paga esse restante.
 */
@Component
public class ValorMinimoPorCartao implements Regra<CartoesDaCompra> {

    static final BigDecimal MINIMO = new BigDecimal("10.00");

    @Override
    public void validar(CartoesDaCompra alvo, Violacoes violacoes) {
        if (alvo.comCupom() && alvo.restante().valor().compareTo(MINIMO) < 0) {
            if (alvo.cartoes().size() > 1) {
                violacoes.adicionar(
                        "RN-VEN-005", "Com cupom e restante abaixo de R$ 10,00, use um único cartão para o restante.");
            }
            return;
        }
        for (PagamentoEmCartao cartao : alvo.cartoes()) {
            if (cartao.valor() != null && cartao.valor().compareTo(MINIMO) < 0) {
                violacoes.adicionar("RN-VEN-004", "Valor mínimo de R$ 10,00 por cartão.");
                return;
            }
        }
    }
}
