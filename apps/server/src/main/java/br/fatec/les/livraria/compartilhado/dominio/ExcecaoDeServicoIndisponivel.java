package br.fatec.les.livraria.compartilhado.dominio;

/**
 * Um serviço externo de que a operação depende não conseguiu atender. Respondida pela API com HTTP 503, com a
 * mensagem da exceção no {@code detail}.
 */
public class ExcecaoDeServicoIndisponivel extends RuntimeException {

    public ExcecaoDeServicoIndisponivel(String mensagem) {
        super(mensagem);
    }
}
