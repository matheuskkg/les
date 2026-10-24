package br.fatec.les.livraria.acesso.infraestrutura.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo de {@code POST /api/sessao} (DEC-08, DEC-10): o login é o e-mail (com {@code @}) ou o telefone, e a senha é
 * obrigatória. Campo em branco responde 400, como os demais parâmetros inválidos (DEC-07).
 */
public record EntradaDeLogin(
        @NotBlank(message = "Informe o login.") String login,
        @NotBlank(message = "Informe a senha.") String senha) {
}
