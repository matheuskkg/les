package br.fatec.les.livraria.vendas.dominio.regra;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;

/** Cada item do pedido aparece uma única vez na troca, com a quantidade total dele (RF-VEN-011, DEC-12). */
@Component
public class ItensDaTrocaSemRepeticao implements Regra<SolicitacaoDeTroca> {

    @Override
    public void validar(SolicitacaoDeTroca alvo, Violacoes violacoes) {
        Set<Long> vistos = new HashSet<>();
        boolean repetido = alvo.itens().stream()
                .filter(item -> item.itemPedidoId() != null)
                .anyMatch(item -> !vistos.add(item.itemPedidoId()));
        if (repetido) {
            violacoes.adicionar("RF-VEN-011", "Cada item só pode aparecer uma vez na troca.");
        }
    }
}
