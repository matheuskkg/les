package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

/** Corpo de {@code POST /api/me/carrinho/itens}: livro e unidades a somar (RF-VEN-001). */
public record AdicaoAoCarrinho(Long livroId, int quantidade) {}
