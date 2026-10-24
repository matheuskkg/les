package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

/** Projeção de um autor por livro, carregada em lote para a vitrine (RF-LIV-005). */
public interface AutorDoLivroProjecao {

    Long getLivroId();

    String getNome();
}
