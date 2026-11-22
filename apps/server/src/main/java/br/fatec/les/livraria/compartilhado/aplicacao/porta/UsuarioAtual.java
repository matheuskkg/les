package br.fatec.les.livraria.compartilhado.aplicacao.porta;

import java.util.Optional;

/**
 * Usuário responsável por uma escrita, usado pela auditoria do Envers (RNF-GER-002, DEC-08).
 *
 * <p>Dentro de uma requisição autenticada devolve o id e o e-mail do usuário; sem login devolve {@code anonimo}; fora
 * de uma requisição HTTP devolve {@code sistema}.
 */
public interface UsuarioAtual {

    String ANONIMO = "anonimo";
    String SISTEMA = "sistema";

    /** Id e e-mail do autor resolvidos de uma só vez, para que a revisão não leia o contexto duas vezes. */
    Autor autor();

    /** Id do usuário autenticado; vazio quando não há login ou fora de uma requisição HTTP. */
    default Optional<Long> usuarioId() {
        return Optional.ofNullable(autor().usuarioId());
    }

    /** E-mail do usuário autenticado, {@link #ANONIMO} ou {@link #SISTEMA}. */
    default String usuario() {
        return autor().usuario();
    }
}
