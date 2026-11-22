package br.fatec.les.livraria.compartilhado.aplicacao;

/** Valida os intervalos {@code De}/{@code Ate} informados nas consultas (DEC-07). */
public final class Intervalo {

    private Intervalo() {}

    /** Lança {@link ParametroInvalido} quando {@code de} é maior que {@code ate}. */
    public static <T extends Comparable<? super T>> void validar(String campo, T de, T ate) {
        if (de != null && ate != null && de.compareTo(ate) > 0) {
            throw new ParametroInvalido(campo, "O início do intervalo de " + campo + " não pode ser maior que o fim.");
        }
    }
}
