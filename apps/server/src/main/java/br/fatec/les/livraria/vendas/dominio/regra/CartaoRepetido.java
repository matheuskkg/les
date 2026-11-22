package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.CartoesDaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.PagamentoEmCartao;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/** Um mesmo cartão não aparece duas vezes no pagamento (RN-VEN-004, DEC-11), salvo ou informado de novo. */
@Component
public class CartaoRepetido implements Regra<CartoesDaCompra> {

    @Override
    public void validar(CartoesDaCompra alvo, Violacoes violacoes) {
        Set<String> numeros = new HashSet<>();
        for (PagamentoEmCartao cartao : alvo.cartoes()) {
            if (!numeros.add(cartao.numero())) {
                violacoes.adicionar("RN-VEN-004", "Um mesmo cartão foi informado mais de uma vez.");
                return;
            }
        }
    }
}
