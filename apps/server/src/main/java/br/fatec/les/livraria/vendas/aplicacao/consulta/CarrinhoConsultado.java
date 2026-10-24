package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Retrato do carrinho para a API (RF-VEN-001, DEC-07). {@code expiraEm} serve só para exibição: o
 * {@code web} não calcula prazos (DEC-13). O subtotal soma os subtotais dos itens e
 * {@code quantidadeDeItens} soma as unidades.
 *
 * @param itens             linhas do carrinho, na ordem de inclusão
 * @param subtotal          soma dos subtotais, zero quando o carrinho está vazio
 * @param quantidadeDeItens soma das quantidades dos itens
 * @param expiraEm          fim do bloqueio do carrinho, nulo quando não há itens
 * @param removidos         livros retirados por expiração, ainda não readicionados nem dispensados (RNF-VEN-001)
 */
public record CarrinhoConsultado(
        List<ItemDoCarrinho> itens,
        Dinheiro subtotal,
        int quantidadeDeItens,
        @Nullable LocalDateTime expiraEm,
        List<ItemRemovidoDoCarrinho> removidos) {

    public CarrinhoConsultado {
        Objects.requireNonNull(itens, "itens");
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(removidos, "removidos");
        itens = List.copyOf(itens);
        removidos = List.copyOf(removidos);
    }
}
