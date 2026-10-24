package br.fatec.les.livraria.vendas.dominio.pagamento;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;

/**
 * Itens do checkout e as unidades que o {@code estoque} ainda reserva para ele, por livro (RN-VEN-002, DEC-12).
 *
 * @param reservadas quantidade reservada por livro; livro sem reserva ativa fica de fora
 */
public record ReservasDoCheckout(List<ItemCheckout> itens, Map<Long, Integer> reservadas) {

    public ReservasDoCheckout {
        itens = List.copyOf(Objects.requireNonNull(itens, "itens"));
        reservadas = Map.copyOf(Objects.requireNonNull(reservadas, "reservadas"));
    }
}
