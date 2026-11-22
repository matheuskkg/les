package br.fatec.les.livraria.vendas.aplicacao;

import br.fatec.les.livraria.estoque.aplicacao.casodeuso.LiberarReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Libera no {@code estoque} as reservas de todos os itens do carrinho (RN-VEN-014, RN-VEN-015, DEC-10, DEC-15.3), na
 * expiração e na inativação da conta. A linha é única por livro, então cada liberação devolve todas as unidades do
 * produto.
 */
@Component
@RequiredArgsConstructor
public class LiberarItensDoCarrinho {

    private final LiberarReserva liberarReserva;

    public void executar(Carrinho carrinho) {
        for (ItemCarrinho item : carrinho.getItens()) {
            liberarReserva.executar(OrigemReserva.CARRINHO, carrinho.getId(), item.getLivroId());
        }
    }
}
