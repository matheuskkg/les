package br.fatec.les.livraria.acesso.dominio.modelo;

/**
 * Dados de senha informados em uma operação (RNF-CLI-001, RNF-CLI-002). O cadastro do cliente e a alteração de senha
 * usam o mesmo par de regras puras do módulo {@code acesso}, sem que o domínio precise conhecer o comando de cada
 * operação.
 */
public interface SenhaInformada {

    /** Senha principal da operação: a senha do cadastro ou a nova senha da alteração. */
    String senha();

    /** Repetição da senha, conferida por {@code ConfirmacaoDeSenha} (RNF-CLI-002). */
    String confirmacaoSenha();
}
