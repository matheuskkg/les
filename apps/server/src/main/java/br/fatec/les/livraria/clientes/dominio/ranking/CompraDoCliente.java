package br.fatec.les.livraria.clientes.dominio.ranking;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Pedido do cliente que conta para o ranking (RN-CLI-007, DEC-04, DEC-12).
 *
 * @param realizadoEm data e hora da finalização, no fuso da loja
 * @param valor total do pedido menos os cupons gerados pelas trocas concluídas dele
 */
public record CompraDoCliente(LocalDateTime realizadoEm, Dinheiro valor) {

    public CompraDoCliente {
        Objects.requireNonNull(realizadoEm, "realizadoEm");
        Objects.requireNonNull(valor, "valor");
    }
}
