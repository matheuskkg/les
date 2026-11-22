package br.fatec.les.livraria.recomendacao.dominio.modelo;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.math.BigDecimal;
import java.util.List;

/**
 * Livro ativo e com estoque que pode ser recomendado (RNF-REC-001, DEC-03). A pontuação é calculada pelo server e
 * define a ordem enviada à IA; o preço é o de venda, já apurado pelo catálogo.
 */
public record LivroCandidato(
        Long id, String titulo, List<String> autores, List<String> categorias, Dinheiro preco, BigDecimal pontuacao) {

    public LivroCandidato {
        autores = List.copyOf(autores);
        categorias = List.copyOf(categorias);
    }
}
