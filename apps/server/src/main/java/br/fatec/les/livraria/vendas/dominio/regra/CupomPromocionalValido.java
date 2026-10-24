package br.fatec.les.livraria.vendas.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.PromocionalNaCompra;

/**
 * Promocional válido para a compra (RN-VEN-007, DEC-11, DEC-15.2): dentro da validade (até o fim do dia), com o
 * subtotal dos itens no valor mínimo, se houver, e ainda não usado pelo cliente.
 */
@Component
public class CupomPromocionalValido implements Regra<PromocionalNaCompra> {

    private static final String CODIGO = "RN-VEN-007";

    @Override
    public void validar(PromocionalNaCompra alvo, Violacoes violacoes) {
        String codigo = alvo.cupom().codigo();
        if (alvo.cupom().vencidoEm(alvo.hoje())) {
            violacoes.adicionar(CODIGO, "O cupom " + codigo + " está vencido.");
        }
        if (!alvo.cupom().atendeValorMinimo(alvo.subtotal())) {
            violacoes.adicionar(CODIGO, "O cupom " + codigo + " exige compra mínima de "
                    + alvo.cupom().valorMinimo().emReais() + " em livros.");
        }
        if (alvo.jaUsado()) {
            violacoes.adicionar(CODIGO, "Você já usou o cupom " + codigo + ".");
        }
    }
}
