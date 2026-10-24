package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

/**
 * Item da consulta de clientes do admin (RF-CLI-004, RNF-CLI-005, DEC-07). O {@code id} é o código exibido do
 * cliente (DEC-14) e não é um filtro. O ranking não faz parte desta listagem (DEC-15.4).
 */
public record ClienteListadoDto(
        Long id,
        String nome,
        String cpf,
        Genero genero,
        LocalDate dataNascimento,
        String email,
        @Nullable String telefone,
        SituacaoConta situacao) {
}
