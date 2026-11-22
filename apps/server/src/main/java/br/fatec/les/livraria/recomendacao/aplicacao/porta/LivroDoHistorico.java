package br.fatec.les.livraria.recomendacao.aplicacao.porta;

/** Livro comprado pelo cliente, como a tool {@code verMeuHistorico} o devolve (RNF-REC-001, DEC-03). */
public record LivroDoHistorico(Long id, String titulo) {}
