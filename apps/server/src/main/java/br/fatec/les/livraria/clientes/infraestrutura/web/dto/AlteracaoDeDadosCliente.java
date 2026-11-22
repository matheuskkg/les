package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * Corpo de {@code PUT /api/me} (RF-CLI-002, RN-CLI-006, DEC-07, DEC-10). Altera nome, gênero, data de nascimento, CPF,
 * e-mail e telefone. O CPF é opcional: omitido, o atual é mantido. Senha, endereços e cartões têm operações próprias.
 */
public record AlteracaoDeDadosCliente(
        String nome,
        Genero genero,
        LocalDate dataNascimento,
        @Nullable String cpf,
        String email,
        TelefoneDoCadastro telefone) {}
