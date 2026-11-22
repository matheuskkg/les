package br.fatec.les.livraria.vendas.aplicacao.porta;

import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;

import java.util.Optional;

/** Persistência do pedido e das suas cópias (RF-VEN-008, DEC-12, DEC-16). */
public interface PedidoRepositorio {

    /**
     * Grava o pedido. Na primeira gravação cria as linhas de itens, cartões e cupons, que não mudam depois; nas
     * seguintes, só o status e a data da entrega.
     */
    Pedido salvar(Pedido pedido);

    /** Pedido pelo id, travado até o fim da transação, para mudar o status (DEC-13). */
    Optional<Pedido> buscarParaAlterar(Long pedidoId);

    /** Pedido {@code EM PROCESSAMENTO} do checkout, travado até o fim da transação; há no máximo um (DEC-13). */
    Optional<Pedido> buscarEmProcessamentoDoCheckout(Long checkoutId);
}
