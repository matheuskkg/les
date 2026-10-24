package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

/** Projeção de uma categoria por livro, carregada em lote para a vitrine (RF-LIV-005, RN-LIV-002). */
public interface CategoriaDoLivroProjecao {

    Long getLivroId();

    Long getCategoriaId();

    String getNome();
}
