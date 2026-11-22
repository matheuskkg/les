package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

/** Projeção da sinopse de um livro pelo id, sem carregar a entidade (RNF-REC-001, RNF-GER-001). */
public interface SinopseDoLivroProjecao {

    Long getLivroId();

    String getSinopse();
}
