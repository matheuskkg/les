package br.fatec.les.livraria.vendas.dominio.regra;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.ItemTroca;
import br.fatec.les.livraria.vendas.dominio.troca.RecebimentoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.RecebimentoDeTroca.ItemRecebido;

/**
 * No recebimento, o admin informa uma única vez, para cada item da troca e só para eles, se o item volta ao estoque
 * (RF-VEN-014, DEC-12).
 */
@Component
public class RetornoAoEstoqueDeCadaItem implements Regra<RecebimentoDeTroca> {

    @Override
    public void validar(RecebimentoDeTroca alvo, Violacoes violacoes) {
        Set<Long> daTroca = alvo.troca().getItens().stream().map(ItemTroca::itemPedidoId).collect(Collectors.toSet());
        Set<Long> informados = alvo.itens().stream()
                .filter(item -> item.voltaEstoque() != null)
                .map(ItemRecebido::itemPedidoId)
                .collect(Collectors.toSet());
        boolean repetido = informados.size() != alvo.itens().size();
        if (repetido || !informados.equals(daTroca)) {
            violacoes.adicionar("RF-VEN-014", "Informe, uma vez para cada item da troca, se ele volta ao estoque.");
        }
    }
}
