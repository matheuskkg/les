package br.fatec.les.livraria.recomendacao.aplicacao.regra;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;

/**
 * Livro que pode ser recomendado ou citado agora: com preço e com estoque disponível (RNF-REC-001, DEC-03,
 * DEC-15.3).
 */
public final class LivroAVenda {

    private LivroAVenda() {
    }

    public static boolean aVenda(LivroListado livro) {
        return livro.precoVenda() != null && livro.disponivel() > 0;
    }
}
