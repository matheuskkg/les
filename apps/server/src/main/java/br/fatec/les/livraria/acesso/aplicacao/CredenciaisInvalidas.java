package br.fatec.les.livraria.acesso.aplicacao;

/**
 * Login recusado (DEC-15.4). A mesma exceção e a mesma mensagem valem para login desconhecido, senha errada e conta
 * encerrada, para não revelar qual identificador existe; o adapter web a responderá com HTTP 401.
 */
public class CredenciaisInvalidas extends RuntimeException {

    public static final String MENSAGEM = "Login ou senha inválidos.";

    public CredenciaisInvalidas() {
        super(MENSAGEM);
    }
}
