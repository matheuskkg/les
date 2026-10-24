package br.fatec.les.livraria.acesso.aplicacao.porta;

import br.fatec.les.livraria.compartilhado.dominio.Senha;

/**
 * Hash e conferência de senhas (RNF-CLI-003), por BCrypt no adapter. A senha é limitada a
 * {@value br.fatec.les.livraria.compartilhado.dominio.Senha#TAMANHO_MAXIMO_EM_BYTES} bytes em UTF-8, limite do
 * BCrypt; a validação do cadastro usa a mesma constante do domínio.
 */
public interface CodificadorDeSenha {

    String codificar(String senha);

    /**
     * Confere a senha com o hash. Uma senha nula ou acima de {@link Senha#TAMANHO_MAXIMO_EM_BYTES} bytes UTF-8
     * devolve {@code false} em vez de deixar o BCrypt lançar.
     */
    boolean confere(String senha, String senhaHash);
}
