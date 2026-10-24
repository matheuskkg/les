package br.fatec.les.livraria.vendas.dominio.regra;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.CartoesDaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.PagamentoEmCartao;

/**
 * Valor de cada cartão (RN-VEN-004, DEC-11): informado, positivo e em centavos inteiros. Não é arredondado, para que
 * R$ 9,995 não passe pelo mínimo de R$ 10,00.
 */
@Component
public class ValorInformadoDoCartao implements Regra<CartoesDaCompra> {

    @Override
    public void validar(CartoesDaCompra alvo, Violacoes violacoes) {
        for (PagamentoEmCartao cartao : alvo.cartoes()) {
            BigDecimal valor = cartao.valor();
            if (valor == null || valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 2) {
                violacoes.adicionar("RN-VEN-004", "Informe um valor positivo, em reais e centavos, para cada cartão.");
                return;
            }
        }
    }
}
