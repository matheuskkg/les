package br.fatec.les.livraria.compartilhado.dominio;

/**
 * Limites da senha do usuário (RNF-CLI-003). Ficam no domínio compartilhado para que a validação do cadastro e o
 * codificador de senhas usem a mesma regra sem o domínio depender da aplicação.
 */
public final class Senha {

    /** Limite do BCrypt, em bytes UTF-8. */
    public static final int TAMANHO_MAXIMO_EM_BYTES = 72;

    private Senha() {
    }
}
