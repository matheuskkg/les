package br.fatec.les.livraria.recomendacao.aplicacao;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeServicoIndisponivel;

/**
 * Nenhum provedor de IA conseguiu atender a solicitação (RNF-REC-001, DEC-03). Não é uma violação de regra de
 * negócio: a vitrine cai na ordem da pontuação e o chat responde HTTP 503.
 */
public class AssistenteIndisponivel extends ExcecaoDeServicoIndisponivel {

    public static final String MENSAGEM = "Assistente indisponível. Tente de novo mais tarde.";

    public AssistenteIndisponivel() {
        super(MENSAGEM);
    }
}
