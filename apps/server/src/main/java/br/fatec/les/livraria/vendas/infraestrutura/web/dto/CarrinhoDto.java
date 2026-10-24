package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;

/**
 * Carrinho na API (RF-VEN-001, DEC-07). {@code expiraEm} é só para exibição (DEC-13), em UTC; ausente quando o
 * carrinho está vazio. {@code removidos} traz os livros retirados por expiração (RNF-VEN-001).
 */
public record CarrinhoDto(
        List<ItemComPrecoDto> itens,
        Dinheiro subtotal,
        int quantidadeDeItens,
        @Nullable Instant expiraEm,
        List<ItemRemovidoDoCarrinhoDto> removidos) {

    public static CarrinhoDto de(CarrinhoConsultado carrinho) {
        return new CarrinhoDto(carrinho.itens().stream().map(ItemComPrecoDto::de).toList(), carrinho.subtotal(),
                carrinho.quantidadeDeItens(),
                carrinho.expiraEm() == null ? null : carrinho.expiraEm().atZone(RelogioDoSistema.FUSO).toInstant(),
                carrinho.removidos().stream().map(ItemRemovidoDoCarrinhoDto::de).toList());
    }
}
