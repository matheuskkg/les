package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

/** Projeção da soma das reservas ativas por livro (DEC-15.3, RN-EST-006). */
public interface ReservaAtivaDoLivroProjecao {

    Long getLivroId();

    Long getQuantidade();
}
