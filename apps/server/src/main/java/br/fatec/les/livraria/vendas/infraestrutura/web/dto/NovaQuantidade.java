package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

/** Corpo de {@code PUT /api/me/carrinho/itens/{livroId}}: a nova quantidade do item (RF-VEN-002). */
public record NovaQuantidade(int quantidade) {
}
