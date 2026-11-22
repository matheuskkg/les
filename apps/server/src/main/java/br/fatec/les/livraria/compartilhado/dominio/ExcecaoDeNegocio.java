package br.fatec.les.livraria.compartilhado.dominio;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/** Uma ou mais regras de negócio foram violadas. Respondida pela API com HTTP 422. */
@Getter
public class ExcecaoDeNegocio extends RuntimeException {

    private final List<Violacao> violacoes;

    public ExcecaoDeNegocio(List<Violacao> violacoes) {
        super(violacoes.stream().map(v -> v.codigo() + ": " + v.mensagem()).collect(Collectors.joining("; ")));
        if (violacoes.isEmpty()) {
            throw new IllegalArgumentException("ExcecaoDeNegocio exige ao menos uma violação");
        }
        this.violacoes = List.copyOf(violacoes);
    }

    public ExcecaoDeNegocio(String codigo, String mensagem) {
        this(List.of(new Violacao(codigo, mensagem)));
    }
}
