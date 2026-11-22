package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.util.List;

/**
 * Livro como o carrinho precisa exibi-lo (RF-VEN-001, DEC-07): incluindo os inativos, para que um livro que saiu da
 * vitrine entre a adição e a leitura não faça o carrinho falhar. A situação é exposta para que o {@code vendas} recuse
 * adicionar um livro inativo.
 */
public record LivroParaVenda(Long id, String titulo, List<String> autores, boolean ativo) {

    public LivroParaVenda {
        autores = List.copyOf(autores);
    }
}
