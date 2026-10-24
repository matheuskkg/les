package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

/** Projeção da quantidade em mãos e das reservas ativas por livro (DEC-15.3). */
public interface SaldoDoLivroProjecao {

    Long getLivroId();

    Integer getQuantidade();

    Integer getReservada();
}
