package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;

import org.springframework.stereotype.Component;

/**
 * Quantidade do item no carrinho: no mínimo 1 (RF-VEN-002, RF-VEN-001). A remoção é uma operação própria; quantidade
 * zero não é aceita. A validação é pura, sem consultar dados.
 */
@Component
public class QuantidadeDoItem implements Regra<ItemProposto> {

    @Override
    public void validar(ItemProposto alvo, Violacoes violacoes) {
        if (alvo.quantidade() < ItemProposto.QUANTIDADE_MINIMA) {
            violacoes.adicionar("RF-VEN-002", "A quantidade do item deve ser no mínimo 1.");
        }
    }
}
