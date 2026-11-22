package br.fatec.les.livraria.vendas.aplicacao.porta;

import br.fatec.les.livraria.vendas.dominio.pagamento.CobrancaNoCartao;
import br.fatec.les.livraria.vendas.dominio.pagamento.RespostaDaOperadora;

import java.time.LocalDate;

/** Operadora de cartão, chamada na finalização fora de transação (RN-VEN-007, DEC-05.2, DEC-13). */
public interface OperadoraDeCartao {

    /**
     * Avalia a cobrança de um cartão.
     *
     * @param data data da transação, usada para a validade do cartão (DEC-06)
     */
    RespostaDaOperadora cobrar(CobrancaNoCartao cobranca, LocalDate data);
}
