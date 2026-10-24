package br.fatec.les.livraria.acesso.aplicacao.porta;

/**
 * Hash e conferência de senhas (RNF-CLI-003), por BCrypt no adapter. A senha é limitada a
 * {@value #TAMANHO_MAXIMO_EM_BYTES} bytes em UTF-8, limite do BCrypt; a validação do cadastro usará esta constante.
 */
public interface CodificadorDeSenha {

    /** Limite do BCrypt, em bytes UTF-8. */
    int TAMANHO_MAXIMO_EM_BYTES = 72;

    String codificar(String senha);

    /**
     * Confere a senha com o hash. Uma senha nula ou acima de {@link #TAMANHO_MAXIMO_EM_BYTES} bytes UTF-8 devolve
     * {@code false} em vez de deixar o BCrypt lançar.
     */
    boolean confere(String senha, String senhaHash);
}
