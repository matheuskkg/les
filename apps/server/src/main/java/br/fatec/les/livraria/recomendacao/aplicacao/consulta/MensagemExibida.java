package br.fatec.les.livraria.recomendacao.aplicacao.consulta;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;

import java.util.List;
import java.util.Objects;

/**
 * Mensagem da conversa pronta para o widget (RNF-REC-001, DEC-03), com os livros citados que seguem ativos no catálogo,
 * com preço e disponível para os cartões.
 */
public record MensagemExibida(MensagemDaConversa mensagem, List<LivroListado> livros) {

    public MensagemExibida {
        Objects.requireNonNull(mensagem, "mensagem");
        livros = List.copyOf(livros);
    }
}
