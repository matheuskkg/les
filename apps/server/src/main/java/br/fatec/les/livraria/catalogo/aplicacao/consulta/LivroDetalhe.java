package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Livro completo como a consulta o devolve, sem preço nem disponibilidade, que são enriquecidos pelo caso
 * de uso (RF-LIV-005, RN-LIV-001). Só livros ativos chegam aqui. Os dados comuns à vitrine ficam em
 * {@link LivroResumo}, reutilizado no lugar de repetir os campos.
 */
public record LivroDetalhe(
        LivroResumo livro,
        String edicao,
        String isbn,
        Integer paginas,
        String sinopse,
        BigDecimal altura,
        BigDecimal largura,
        BigDecimal profundidade,
        BigDecimal peso,
        String codigoBarras) {

    public LivroDetalhe {
        Objects.requireNonNull(livro, "livro");
    }
}
