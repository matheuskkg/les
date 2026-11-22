package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.TipoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoLogradouro;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;

import org.jspecify.annotations.Nullable;

/** Endereço no detalhe do cliente (RN-CLI-003, RF-CLI-006, DEC-10). O país é sempre "Brasil". */
public record EnderecoDoClienteDto(
        Long id,
        String nomeCurto,
        TipoEndereco tipoEndereco,
        TipoLogradouro tipoLogradouro,
        String logradouro,
        String numero,
        String bairro,
        String cep,
        String cidade,
        Uf uf,
        String pais,
        @Nullable String observacoes) {}
