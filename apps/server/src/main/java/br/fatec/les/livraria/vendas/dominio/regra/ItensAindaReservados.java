package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.ReservasDoCheckout;

import org.springframework.stereotype.Component;

/**
 * A finalização revalida o estoque (RN-VEN-002, DEC-12): cada item do checkout precisa continuar reservado, com a mesma
 * quantidade. Sem a reserva, as unidades podem já ter ido para outro cliente.
 */
@Component
public class ItensAindaReservados implements Regra<ReservasDoCheckout> {

    @Override
    public void validar(ReservasDoCheckout alvo, Violacoes violacoes) {
        boolean todosReservados =
                alvo.itens().stream()
                        .allMatch(item ->
                                Integer.valueOf(item.quantidade()).equals(alvo.reservadas().get(item.livroId())));
        if (!todosReservados) {
            violacoes.adicionar(
                    "RN-VEN-002",
                    "Os itens da compra não estão mais reservados. Volte ao carrinho e " + "confira o estoque.");
        }
    }
}
