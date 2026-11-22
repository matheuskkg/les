package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

/** Projeção do título e da situação de um livro pelo id, sem carregar a entidade (RF-VEN-001, RNF-GER-001). */
public interface LivroParaVendaProjecao {

    Long getLivroId();

    String getTitulo();

    boolean isAtivo();
}
