package br.fatec.les.livraria.compartilhado.aplicacao;

import java.util.List;

/**
 * Parâmetros de uma listagem paginada (DEC-07, RNF-GER-001).
 *
 * @param pagina página a partir de 0
 * @param tamanho itens por página, entre 1 e {@value #TAMANHO_MAXIMO}
 * @param ordem critérios de ordenação, do mais para o menos significativo
 */
public record Paginacao(int pagina, int tamanho, List<Ordem> ordem) {

    public static final int PAGINA_PADRAO = 0;
    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;

    public Paginacao {
        if (pagina < 0) {
            throw new ParametroInvalido("pagina", "A página não pode ser negativa.");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new ParametroInvalido("tamanho", "O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
        }
        if ((long) pagina * tamanho > Integer.MAX_VALUE) {
            throw new ParametroInvalido("pagina", "A página informada é grande demais para o tamanho escolhido.");
        }
        ordem = ordem == null ? List.of() : List.copyOf(ordem);
    }

    /** Paginação sem critérios de ordenação, com os valores padrão. */
    public static Paginacao padrao() {
        return new Paginacao(PAGINA_PADRAO, TAMANHO_PADRAO, List.of());
    }
}
