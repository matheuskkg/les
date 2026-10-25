package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.util.List;

/**
 * Resposta da operação {@code recomendar}, com os livros na ordem em que a vitrine deve exibi-los (RNF-REC-001,
 * DEC-03). A ordem é decidida pela IA e validada pelo server antes de ser gravada.
 */
public record RespostaDeRecomendacao(List<ItemRecomendado> itens) {

    public RespostaDeRecomendacao {
        itens = List.copyOf(itens);
    }
}
