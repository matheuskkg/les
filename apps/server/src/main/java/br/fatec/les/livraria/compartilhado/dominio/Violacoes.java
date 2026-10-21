package br.fatec.les.livraria.compartilhado.dominio;

import java.util.ArrayList;
import java.util.List;

/** Acumula as violações encontradas pelas regras de uma operação. */
public final class Violacoes {

    private final List<Violacao> itens = new ArrayList<>();

    /**
     * Executa todas as regras sobre {@code alvo} e lança {@link ExcecaoDeNegocio} com a lista completa, se alguma
     * regra registrar violação.
     */
    @SafeVarargs
    public static <T> void verificar(T alvo, Regra<? super T>... regras) {
        Violacoes violacoes = new Violacoes();
        for (Regra<? super T> regra : regras) {
            regra.validar(alvo, violacoes);
        }
        violacoes.lancarSeHouver();
    }

    public void adicionar(String codigo, String mensagem) {
        itens.add(new Violacao(codigo, mensagem));
    }

    public boolean vazia() {
        return itens.isEmpty();
    }

    public List<Violacao> lista() {
        return List.copyOf(itens);
    }

    public void lancarSeHouver() {
        if (!itens.isEmpty()) {
            throw new ExcecaoDeNegocio(lista());
        }
    }
}
