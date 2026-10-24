package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;

/** Endereço de entrega para o cálculo do frete do checkout (RF-VEN-004, RF-VEN-005). */
public record PedidoDeFrete(EscolhaDeEnderecoDto entrega) {

    public EscolhaDeEndereco paraEscolha() {
        return entrega == null ? null : entrega.paraEscolha();
    }
}
