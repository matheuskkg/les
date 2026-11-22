package br.fatec.les.livraria.vendas.aplicacao;

import br.fatec.les.livraria.estoque.aplicacao.casodeuso.LiberarReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Libera no {@code estoque} as reservas de todos os itens do checkout (RN-VEN-014, DEC-10, DEC-12): na volta ao
 * carrinho, na expiração e na inativação da conta.
 */
@Component
@RequiredArgsConstructor
public class LiberarItensDoCheckout {

    private final LiberarReserva liberarReserva;

    public void executar(Checkout checkout) {
        for (ItemCheckout item : checkout.getItens()) {
            liberarReserva.executar(OrigemReserva.CHECKOUT, checkout.getId(), item.livroId());
        }
    }
}
