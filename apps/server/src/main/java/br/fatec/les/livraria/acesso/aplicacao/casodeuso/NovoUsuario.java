package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;

/**
 * Comando de criação de um usuário de cliente (DEC-08, DEC-10): e-mail, telefone e senha em texto, que o caso de uso
 * codifica antes de gravar (RNF-CLI-003).
 */
public record NovoUsuario(String email, TipoTelefone tipoTelefone, String ddd, String numero, String senha) {
}
