package br.fatec.les.livraria.clientes.dominio.modelo;

import br.fatec.les.livraria.clientes.dominio.modelo.TipoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoLogradouro;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;

/**
 * Endereço informado no cadastro do cliente (RN-CLI-003, DEC-10). O país não vem do cliente: é sempre "Brasil".
 */
public record NovoEndereco(
        String nomeCurto,
        TipoEndereco tipoEndereco,
        TipoLogradouro tipoLogradouro,
        String logradouro,
        String numero,
        String bairro,
        String cep,
        String cidade,
        Uf uf,
        String observacoes) {
}
