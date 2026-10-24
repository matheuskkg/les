package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.util.Objects;

import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/** Cartão conferido no pagamento e o valor que ele paga (RF-VEN-006, RN-VEN-004, DEC-11). */
public record CartaoNoPagamento(Cartao cartao, Dinheiro valor) {

    public CartaoNoPagamento {
        Objects.requireNonNull(cartao, "cartao");
        Objects.requireNonNull(valor, "valor");
    }
}
