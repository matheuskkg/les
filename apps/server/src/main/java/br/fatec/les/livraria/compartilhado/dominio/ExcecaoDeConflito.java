package br.fatec.les.livraria.compartilhado.dominio;

/** A operação conflita com o estado atual do recurso. A API responde HTTP 409 (DEC-07). */
public class ExcecaoDeConflito extends RuntimeException {

    public ExcecaoDeConflito(String mensagem) {
        super(mensagem);
    }

    public ExcecaoDeConflito(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
