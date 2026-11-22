package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * Item da consulta de clientes do admin (RF-CLI-004, RNF-CLI-005, DEC-07). O {@code id} é o código exibido do cliente
 * (DEC-14) e não é um filtro. O ranking é só exibido; a consulta não ordena por ele (RN-CLI-007, DEC-15.4).
 */
public record ClienteListadoDto(
        Long id,
        String nome,
        String cpf,
        Genero genero,
        LocalDate dataNascimento,
        String email,
        @Nullable String telefone,
        SituacaoConta situacao,
        int ranking) {}
