package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAutenticado;

/** Principal da sessão (DEC-08): o controller e a auditoria leem daqui o id e o e-mail do usuário. */
public record PrincipalDaSessao(Long usuarioId, String email) implements UsuarioAutenticado {
}
