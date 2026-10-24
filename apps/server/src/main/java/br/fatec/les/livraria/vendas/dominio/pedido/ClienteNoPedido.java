package br.fatec.les.livraria.vendas.dominio.pedido;

import java.util.Objects;

/**
 * Cópia do cliente no momento da compra (DEC-12, DEC-16), com o id de origem.
 *
 * @param telefone telefone formatado, como {@code (11) 98888-7001}
 */
public record ClienteNoPedido(Long id, String nome, String cpf, String email, String telefone) {

    public ClienteNoPedido {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(nome, "nome");
        Objects.requireNonNull(cpf, "cpf");
        Objects.requireNonNull(email, "email");
    }
}
