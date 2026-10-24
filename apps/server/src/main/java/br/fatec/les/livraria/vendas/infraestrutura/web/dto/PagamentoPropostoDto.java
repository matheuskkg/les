package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.vendas.aplicacao.casodeuso.PagamentoInformado;

/**
 * Forma de pagamento proposta no checkout (RF-VEN-006, RF-VEN-007, DEC-11): endereço de entrega, cupons e cartões.
 * Sem cartões, a resposta mostra o restante que eles terão de pagar.
 */
public record PagamentoPropostoDto(
        EscolhaDeEnderecoDto entrega,
        @Nullable List<String> cuponsPromocionais,
        @Nullable List<Long> cuponsDeTroca,
        @Nullable List<CartaoDoPagamentoDto> cartoes) {

    public PagamentoInformado paraPagamento() {
        return new PagamentoInformado(entrega == null ? null : entrega.paraEscolha(), cuponsPromocionais,
                cuponsDeTroca,
                cartoes == null ? null : cartoes.stream()
                        .map(cartao -> cartao == null ? null : cartao.paraCartaoInformado())
                        .toList());
    }
}
