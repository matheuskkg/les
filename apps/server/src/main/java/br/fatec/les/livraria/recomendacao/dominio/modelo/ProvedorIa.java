package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Linha da cadeia de provedores de IA (RNF-REC-001, DEC-03), na tabela {@code provedor_ia}. A ordem define a
 * preferência e o {@code ativo} permite tirar um provedor da cadeia sem apagar a configuração.
 */
public record ProvedorIa(Long id, String nome, int ordem, boolean ativo) {}
