package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.util.List;

/**
 * Resposta do chatbot, sempre com o tipo e os livros citados (RNF-REC-001, DEC-03). Os ids são validados pelo
 * server: livro inexistente, inativo ou sem estoque é removido antes de a resposta ser gravada.
 */
public record RespostaDoAssistente(
        TipoDeResposta tipo,
        String texto,
        List<Long> livrosCitados) {

    public RespostaDoAssistente {
        livrosCitados = List.copyOf(livrosCitados);
    }
}
