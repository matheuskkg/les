package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;

import org.jspecify.annotations.Nullable;

/**
 * Endereço escolhido na compra (RF-VEN-005, DEC-10): {@code enderecoId} de um endereço cadastrado ou {@code novo},
 * nunca os dois.
 */
public record EscolhaDeEnderecoDto(@Nullable Long enderecoId, @Nullable EnderecoInformadoDto novo) {

    public EscolhaDeEndereco paraEscolha() {
        return new EscolhaDeEndereco(enderecoId, novo == null ? null : novo.paraNovoEndereco());
    }
}
