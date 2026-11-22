package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.CuponsDaCompra;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Cupom desnecessário (RN-VEN-006, DEC-11): um cupom, promocional ou de troca, é desnecessário se, sem ele, os demais
 * ainda cobrem o total. Havendo algum, a compra é recusada indicando quais, e o cliente escolhe o que retirar. O
 * promocional conta pelo desconto que dá nesta compra.
 */
@Component
public class CuponsDesnecessarios implements Regra<CuponsDaCompra> {

    @Override
    public void validar(CuponsDaCompra compra, Violacoes violacoes) {
        List<String> codigos = new ArrayList<>();
        List<Dinheiro> coberturas = new ArrayList<>();
        if (compra.promocional() != null) {
            codigos.add(compra.promocional().codigo());
            coberturas.add(compra.descontoPromocional());
        }
        for (CupomTroca cupom : compra.cuponsDeTroca()) {
            codigos.add(cupom.codigo());
            coberturas.add(cupom.valor());
        }

        Dinheiro soma = coberturas.stream().reduce(Dinheiro.ZERO, Dinheiro::somar);
        for (int i = 0; i < codigos.size(); i++) {
            if (!soma.subtrair(coberturas.get(i)).menorQue(compra.total())) {
                violacoes.adicionar(
                        "RN-VEN-006",
                        "O cupom " + codigos.get(i) + " é desnecessário: sem ele, os demais já cobrem o total.");
            }
        }
    }
}
