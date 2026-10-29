package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import java.math.BigDecimal;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.DetalheDoLivro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.LivroDoHistorico;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;

/**
 * Tools só de leitura expostas ao modelo pelo Spring AI (RNF-REC-001, DEC-03). Delegam a
 * {@link FerramentasDoAssistente}; nenhuma recebe o id do cliente, que vem sempre do {@code UsuarioAtual}.
 */
class FerramentasParaIa {

    private final FerramentasDoAssistente ferramentas;

    FerramentasParaIa(FerramentasDoAssistente ferramentas) {
        this.ferramentas = ferramentas;
    }

    @Tool(description = "Busca livros à venda por texto no título, no autor ou na sinopse, com filtros opcionais "
            + "de categoria e de faixa de preço em reais.")
    List<LivroParaIa> buscarLivros(
            @ToolParam(description = "Texto procurado no título, no autor ou na sinopse") String texto,
            @ToolParam(description = "Nome da categoria", required = false) @Nullable String categoria,
            @ToolParam(description = "Preço mínimo em reais", required = false) @Nullable BigDecimal precoMinimo,
            @ToolParam(description = "Preço máximo em reais", required = false) @Nullable BigDecimal precoMaximo) {
        return ferramentas.buscarLivros(texto, categoria, precoMinimo, precoMaximo).stream()
                .map(LivroParaIa::de)
                .toList();
    }

    @Tool(description = "Mostra o detalhe de um livro à venda pelo id, com a sinopse e a quantidade disponível. "
            + "Devolve vazio se o livro não existir ou não estiver à venda.")
    @Nullable DetalheParaIa verLivro(@ToolParam(description = "Id do livro") Long livroId) {
        return ferramentas.verLivro(livroId).map(DetalheParaIa::de).orElse(null);
    }

    @Tool(description = "Lista os livros que o cliente já comprou na loja.")
    List<LivroDoHistorico> verMeuHistorico() {
        return ferramentas.verMeuHistorico();
    }

    @Tool(description = "Lista livros recomendados para o cliente pelo histórico, já sem os livros que ele comprou.")
    List<LivroParaIa> recomendarParaMim() {
        return ferramentas.recomendarParaMim().stream()
                .map(LivroParaIa::de)
                .toList();
    }

    /** Livro como o modelo o recebe, com o preço como número. */
    record LivroParaIa(Long id, String titulo, List<String> autores, List<String> categorias, BigDecimal preco) {

        static LivroParaIa de(ResumoDeLivro livro) {
            return new LivroParaIa(livro.id(), livro.titulo(), livro.autores(), livro.categorias(),
                    livro.preco().valor());
        }
    }

    /** Detalhe do livro como o modelo o recebe. */
    record DetalheParaIa(LivroParaIa livro, String sinopse, int disponivel) {

        static DetalheParaIa de(DetalheDoLivro detalhe) {
            return new DetalheParaIa(LivroParaIa.de(detalhe.livro()), detalhe.sinopse(), detalhe.disponivel());
        }
    }
}
