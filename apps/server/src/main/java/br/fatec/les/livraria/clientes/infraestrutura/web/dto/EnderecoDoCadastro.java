package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.TipoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoLogradouro;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;

/**
 * Endereço informado no cadastro e nas rotas {@code /api/me/enderecos} (RN-CLI-003, RF-CLI-006, DEC-10). O país não é
 * editável: é sempre "Brasil".
 */
public record EnderecoDoCadastro(
        String nomeCurto,
        TipoEndereco tipoEndereco,
        TipoLogradouro tipoLogradouro,
        String logradouro,
        String numero,
        String bairro,
        String cep,
        String cidade,
        Uf uf,
        String observacoes) {}
