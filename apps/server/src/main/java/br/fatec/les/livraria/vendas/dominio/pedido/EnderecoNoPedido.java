package br.fatec.les.livraria.vendas.dominio.pedido;

import java.util.Objects;

/**
 * Cópia de um endereço de entrega ou de cobrança no momento da compra (DEC-10, DEC-12, DEC-16). Os tipos e a UF vêm
 * pelo nome, para a cópia não depender das enumerações do cadastro.
 *
 * @param id endereço do cadastro de origem; nulo para um endereço novo não salvo no cadastro
 */
public record EnderecoNoPedido(
        Long id,
        String nomeCurto,
        String tipoEndereco,
        String tipoLogradouro,
        String logradouro,
        String numero,
        String bairro,
        String cep,
        String cidade,
        String uf,
        String pais,
        String observacoes) {

    public EnderecoNoPedido {
        Objects.requireNonNull(logradouro, "logradouro");
        Objects.requireNonNull(cep, "cep");
    }
}
