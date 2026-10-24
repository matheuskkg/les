package br.fatec.les.livraria.acesso.aplicacao.porta;

/** Hash e conferência de senhas (RNF-CLI-003), por BCrypt no adapter. */
public interface CodificadorDeSenha {

    String codificar(String senha);

    boolean confere(String senha, String senhaHash);
}
