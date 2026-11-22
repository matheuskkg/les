package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.DetalheDoLivro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tools só de leitura expostas ao modelo pelo Spring AI (RNF-REC-001, DEC-03). Delegam a {@link
 * FerramentasDoAssistente}; nenhuma recebe o id do cliente, que vem sempre do {@code UsuarioAtual}.
 *
 * <p>O modelo busca pelo catálogo compacto do prompt e só chama {@code verLivro} para sinopse ou estoque e {@code
 * recomendarParaMim} para sugestões pelo histórico (REC-09).
 */
class FerramentasParaIa {

    private final FerramentasDoAssistente ferramentas;

    FerramentasParaIa(FerramentasDoAssistente ferramentas) {
        this.ferramentas = ferramentas;
    }

    @Tool(
            description =
                    "Mostra o detalhe de um livro à venda pelo id, com a sinopse e a quantidade disponível. "
                            + "Devolve vazio se o livro não existir ou não estiver à venda.")
    @Nullable DetalheParaIa verLivro(@ToolParam(description = "Id do livro") Long livroId) {
        return ferramentas.verLivro(livroId).map(DetalheParaIa::de).orElse(null);
    }

    @Tool(description = "Lista livros recomendados para o cliente pelo histórico, já sem os livros que ele comprou.")
    List<LivroParaIa> recomendarParaMim() {
        return ferramentas.recomendarParaMim().stream().map(LivroParaIa::de).toList();
    }

    /** Livro como o modelo o recebe, com o preço como número. */
    record LivroParaIa(Long id, String titulo, List<String> autores, List<String> categorias, BigDecimal preco) {

        static LivroParaIa de(ResumoDeLivro livro) {
            return new LivroParaIa(
                    livro.id(), livro.titulo(), livro.autores(), livro.categorias(), livro.preco().valor());
        }
    }

    /** Detalhe do livro como o modelo o recebe. */
    record DetalheParaIa(LivroParaIa livro, String sinopse, int disponivel) {

        static DetalheParaIa de(DetalheDoLivro detalhe) {
            return new DetalheParaIa(LivroParaIa.de(detalhe.livro()), detalhe.sinopse(), detalhe.disponivel());
        }
    }
}
