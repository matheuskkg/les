package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

/** Projeção da quantidade em mãos por livro (DEC-15.3). */
public interface QuantidadeDoLivroProjecao {

    Long getLivroId();

    Integer getQuantidade();
}
