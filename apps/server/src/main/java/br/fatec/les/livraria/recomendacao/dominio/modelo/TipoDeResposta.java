package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Tipo da resposta do chatbot, devolvido na saída estruturada (RNF-REC-001, DEC-03). Com {@link #FORA_DO_ESCOPO}, o
 * server grava um texto fixo e descarta o texto do modelo.
 */
public enum TipoDeResposta {
    RECOMENDACAO,
    BUSCA,
    DUVIDA,
    FORA_DO_ESCOPO
}
