package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

/**
 * Detalhe de um cliente para o admin (RF-CLI-004, RNF-CLI-005, DEC-07, DEC-10): dados pessoais, contato, situação da
 * conta, ranking (RN-CLI-007), endereços e cartões mascarados.
 */
public record ClienteDetalheDto(
        Long id,
        String nome,
        String cpf,
        Genero genero,
        LocalDate dataNascimento,
        String email,
        @Nullable String telefone,
        SituacaoConta situacao,
        int ranking,
        List<EnderecoDoClienteDto> enderecos,
        List<CartaoDoClienteDto> cartoes) {}
