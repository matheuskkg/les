package br.fatec.les.livraria.recomendacao.aplicacao;

/**
 * Nenhum provedor de IA conseguiu atender a solicitação (RNF-REC-001, DEC-03). Não é uma violação de regra de
 * negócio: o REC-02 cai na ordem da pontuação e o REC-03 responde HTTP 503.
 */
public class AssistenteIndisponivel extends RuntimeException {

    public static final String MENSAGEM = "Assistente indisponível. Tente de novo mais tarde.";

    public AssistenteIndisponivel() {
        super(MENSAGEM);
    }
}
