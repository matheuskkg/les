package br.fatec.les.livraria.acesso.infraestrutura.web.dto;

/**
 * Corpo de {@code POST /api/me/senha} (RF-CLI-008, RNF-CLI-001, RNF-CLI-002): a senha atual, a nova senha e a
 * confirmação. A sessão atual continua válida após a troca (DEC-08).
 */
public record AlteracaoDeSenha(String senhaAtual, String novaSenha, String confirmacaoSenha) {
}
