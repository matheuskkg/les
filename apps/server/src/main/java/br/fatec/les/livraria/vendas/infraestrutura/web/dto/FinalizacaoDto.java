package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.vendas.aplicacao.casodeuso.FinalizacaoInformada;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * "Finalizar compra" (RF-VEN-005 a RF-VEN-008, DEC-11): endereços de entrega e de cobrança, cupons e cartões, cada um
 * com o código de segurança.
 */
public record FinalizacaoDto(
        EscolhaDeEnderecoDto entrega,
        EscolhaDeEnderecoDto cobranca,
        @Nullable List<String> cuponsPromocionais,
        @Nullable List<Long> cuponsDeTroca,
        @Nullable List<CartaoDaFinalizacaoDto> cartoes) {

    public FinalizacaoInformada paraFinalizacao() {
        List<CartaoDaFinalizacaoDto> informados = cartoes == null ? List.of() : cartoes;
        PagamentoPropostoDto pagamento =
                new PagamentoPropostoDto(
                        entrega,
                        cuponsPromocionais,
                        cuponsDeTroca,
                        informados.stream()
                                .map(cartao -> cartao == null ? null : cartao.paraCartaoDoPagamento())
                                .toList());
        return new FinalizacaoInformada(
                pagamento.paraPagamento(),
                cobranca == null ? null : cobranca.paraEscolha(),
                informados.stream().filter(Objects::nonNull).map(CartaoDaFinalizacaoDto::codigoSeguranca).toList());
    }
}
