package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

/**
 * Tools só de leitura que o chatbot pode chamar (RNF-REC-001, DEC-03). A implementação (REC-03) usa os casos de uso
 * do {@code catalogo}, do {@code estoque} e do {@code vendas}; o id do cliente vem sempre de
 * {@code UsuarioAtual}, nunca de argumento do modelo. A IA não executa ações: adicionar ao carrinho é um botão da
 * UI sobre os livros devolvidos.
 */
public interface FerramentasDoAssistente {

    /**
     * Busca livros ativos e com estoque por texto no título, no autor ou na sinopse, categoria e faixa de preço.
     * Os filtros são opcionais.
     */
    List<ResumoDeLivro> buscarLivros(
            String texto,
            @Nullable String categoria,
            @Nullable BigDecimal precoMinimo,
            @Nullable BigDecimal precoMaximo);

    /** Detalhe de um livro ativo e com estoque, ou vazio se o id não existir. */
    Optional<DetalheDoLivro> verLivro(Long livroId);

    /** Livros que o cliente autenticado já comprou, para o assistente conhecer o histórico. */
    List<LivroDoHistorico> verMeuHistorico();

    /**
     * Livros recomendados ao cliente autenticado pelo histórico, com a mesma recuperação e pontuação da vitrine
     * (RNF-REC-001, DEC-03). Já vêm sem os comprados e sem os avaliados negativamente; sem histórico, os mais
     * vendidos.
     */
    List<ResumoDeLivro> recomendarParaMim();
}
