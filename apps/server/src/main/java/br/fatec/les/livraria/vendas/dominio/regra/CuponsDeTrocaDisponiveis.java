package br.fatec.les.livraria.vendas.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.CuponsDeTrocaNaCompra;

/** Cupons de troca disponíveis (RN-VEN-007, DEC-11): nem usados, nem vencidos. */
@Component
public class CuponsDeTrocaDisponiveis implements Regra<CuponsDeTrocaNaCompra> {

    @Override
    public void validar(CuponsDeTrocaNaCompra alvo, Violacoes violacoes) {
        for (CupomTroca cupom : alvo.cupons()) {
            if (!cupom.disponivelEm(alvo.hoje())) {
                violacoes.adicionar("RN-VEN-007", "O cupom " + cupom.codigo() + " não está disponível.");
            }
        }
    }
}
