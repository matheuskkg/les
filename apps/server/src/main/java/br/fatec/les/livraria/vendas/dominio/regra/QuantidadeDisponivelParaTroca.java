package br.fatec.les.livraria.vendas.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca.ItemSolicitado;

/**
 * Cada item pede entre 1 e a quantidade disponível para troca: a comprada menos a que já está em qualquer troca do
 * pedido, inclusive as recusadas (RF-VEN-011, DEC-12). Itens de fora do pedido ficam com a
 * {@link ItensDaTrocaInformados}.
 */
@Component
public class QuantidadeDisponivelParaTroca implements Regra<SolicitacaoDeTroca> {

    @Override
    public void validar(SolicitacaoDeTroca alvo, Violacoes violacoes) {
        for (ItemSolicitado solicitado : alvo.itens()) {
            alvo.pedido().getItens().stream()
                    .filter(item -> item.id().equals(solicitado.itemPedidoId()))
                    .findFirst()
                    .ifPresent(item -> validar(alvo, item, solicitado.quantidade(), violacoes));
        }
    }

    private static void validar(SolicitacaoDeTroca alvo, ItemPedido item, int quantidade, Violacoes violacoes) {
        int disponivel = alvo.trocas().quantidadeDisponivel(item);
        if (quantidade < 1) {
            violacoes.adicionar("RF-VEN-011", "Informe ao menos 1 unidade de \"%s\" para trocar."
                    .formatted(item.livro().titulo()));
        } else if (quantidade > disponivel) {
            violacoes.adicionar("RF-VEN-011", disponivel == 0
                    ? "\"%s\" não tem mais unidades disponíveis para troca.".formatted(item.livro().titulo())
                    : "Só %d unidade(s) de \"%s\" podem ser trocadas.".formatted(disponivel, item.livro().titulo()));
        }
    }
}
