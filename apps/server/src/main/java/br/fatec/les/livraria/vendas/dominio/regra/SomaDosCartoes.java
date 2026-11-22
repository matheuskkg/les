package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.CartoesDaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.PagamentoEmCartao;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Os cartões pagam exatamente o restante depois dos cupons (RN-VEN-004, DEC-11). Restante zero não aceita cartão; com
 * restante, ao menos um cartão.
 */
@Component
public class SomaDosCartoes implements Regra<CartoesDaCompra> {

    private static final String CODIGO = "RN-VEN-004";

    @Override
    public void validar(CartoesDaCompra alvo, Violacoes violacoes) {
        if (alvo.restante().ehZero()) {
            if (!alvo.cartoes().isEmpty()) {
                violacoes.adicionar(CODIGO, "Os cupons já cobrem o total: retire os cartões.");
            }
            return;
        }
        if (alvo.cartoes().isEmpty()) {
            violacoes.adicionar(CODIGO, "Informe ao menos um cartão para pagar " + alvo.restante().emReais() + ".");
            return;
        }
        BigDecimal soma =
                alvo.cartoes().stream()
                        .map(PagamentoEmCartao::valor)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (soma.compareTo(alvo.restante().valor()) != 0) {
            violacoes.adicionar(CODIGO, "A soma dos cartões deve ser " + alvo.restante().emReais() + ".");
        }
    }
}
