package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

/** Projeção de uma contagem agrupada por status, de pedido ou de troca. */
interface QuantidadePorStatus<S> {

    S getStatus();

    Long getQuantidade();
}
