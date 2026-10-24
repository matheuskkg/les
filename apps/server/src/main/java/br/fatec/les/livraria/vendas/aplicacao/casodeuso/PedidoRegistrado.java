package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pagamento.CobrancaNoCartao;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;

/**
 * Pedido gravado {@code EM PROCESSAMENTO} e o que só existe em memória até o resultado da operadora (DEC-11, DEC-13):
 * as cobranças, com número e código de segurança, e o troco, que vira cupom só na aprovação.
 */
public record PedidoRegistrado(Pedido pedido, List<CobrancaNoCartao> cobrancas, Dinheiro troco) {

    public PedidoRegistrado {
        Objects.requireNonNull(pedido, "pedido");
        cobrancas = List.copyOf(Objects.requireNonNull(cobrancas, "cobrancas"));
        Objects.requireNonNull(troco, "troco");
    }
}
