package br.fatec.les.livraria.compartilhado.dominio;

/** O recurso consultado não existe ou não está visível. A API responde HTTP 404 (DEC-07). */
public class ExcecaoDeRecursoNaoEncontrado extends RuntimeException {

    public ExcecaoDeRecursoNaoEncontrado(String mensagem) {
        super(mensagem);
    }
}
