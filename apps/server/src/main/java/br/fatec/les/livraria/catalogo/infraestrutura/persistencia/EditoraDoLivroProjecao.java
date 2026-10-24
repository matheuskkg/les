package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

/** Projeção do nome da editora por livro, carregada em lote para a vitrine (RF-LIV-005). */
public interface EditoraDoLivroProjecao {

    Long getLivroId();

    String getNome();
}
