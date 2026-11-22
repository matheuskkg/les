package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contexto enxuto do chat (RNF-REC-001, DEC-03): o catálogo à venda em uma linha por livro e o perfil com os ids dos
 * livros comprados, sem repetir títulos e categorias que já estão no catálogo. A vitrine continua usando a descrição
 * completa do perfil, porque não recebe o catálogo.
 */
final class ContextoCompactoDoChat {

    static final String CABECALHO_DO_CATALOGO = "id|titulo|autores|categorias|preco";
    static final int FREQUENTES = 5;

    private ContextoCompactoDoChat() {}

    /** Catálogo compacto: cabeçalho e uma linha {@code id|titulo|autores|categorias|preco} por livro à venda. */
    static String catalogo(List<ResumoDeLivro> livros) {
        return CABECALHO_DO_CATALOGO
                + "\n"
                + livros.stream()
                        .map(livro ->
                                livro.id()
                                        + "|"
                                        + campo(livro.titulo())
                                        + "|"
                                        + campo(String.join(", ", livro.autores()))
                                        + "|"
                                        + campo(String.join(", ", livro.categorias()))
                                        + "|"
                                        + livro.preco().valor().toPlainString())
                        .collect(Collectors.joining("\n"));
    }

    /**
     * Perfil enxuto: ids dos livros comprados que estão no catálogo, título curto dos que não estão, até {@value
     * #FREQUENTES} categorias e autores e os ids das avaliações positivas e negativas.
     */
    static String perfil(PerfilDoCliente perfil, List<ResumoDeLivro> catalogo) {
        Set<Long> idsDoCatalogo = catalogo.stream().map(ResumoDeLivro::id).collect(Collectors.toSet());
        StringBuilder texto = new StringBuilder();
        texto.append("- Livros comprados: ")
                .append(
                        perfil.livrosComprados().isEmpty()
                                ? "nenhum"
                                : perfil.livrosComprados().stream()
                                        .map(livro -> descrever(livro, idsDoCatalogo))
                                        .collect(Collectors.joining(", ")))
                .append('\n');
        texto.append("- Categorias frequentes: ").append(lista(perfil.categoriasFrequentes())).append('\n');
        texto.append("- Autores frequentes: ").append(lista(perfil.autoresFrequentes())).append('\n');
        texto.append("- Avaliações de recomendações: ").append(avaliacoes(perfil.avaliacoes()));
        return texto.toString();
    }

    /** Tira do campo o separador e as quebras de linha, para não desalinhar as colunas que o modelo lê. */
    private static String campo(String valor) {
        return valor.replaceAll("[|\\s]+", " ").strip();
    }

    private static String descrever(LivroComprado livro, Set<Long> idsDoCatalogo) {
        return idsDoCatalogo.contains(livro.id()) ? livro.id().toString() : "\"" + livro.titulo() + "\"";
    }

    private static String avaliacoes(List<AvaliacaoDada> avaliacoes) {
        if (avaliacoes.isEmpty()) {
            return "nenhuma";
        }
        List<Long> positivas = avaliacoes.stream().filter(AvaliacaoDada::positiva).map(AvaliacaoDada::livroId).toList();
        List<Long> negativas =
                avaliacoes.stream().filter(avaliacao -> !avaliacao.positiva()).map(AvaliacaoDada::livroId).toList();
        return "positivas: " + ids(positivas) + "; negativas: " + ids(negativas);
    }

    private static String lista(List<String> itens) {
        if (itens.isEmpty()) {
            return "nenhum";
        }
        return String.join(", ", itens.stream().limit(FREQUENTES).toList());
    }

    private static String ids(List<Long> ids) {
        return ids.isEmpty() ? "nenhuma" : ids.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }
}
