package br.fatec.les.livraria.acesso.infraestrutura.web.dto;

import br.fatec.les.livraria.acesso.dominio.modelo.Papel;

/**
 * Sessão do usuário logado (DEC-08), devolvida no login e na consulta da sessão. O {@code nome} é o de exibição:
 * o do cliente, "Administração" para o administrador ou, na falta dos dois, o e-mail.
 */
public record Sessao(Long usuarioId, String email, Papel papel, String nome) {
}
