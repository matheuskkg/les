package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;

/**
 * Forma de pagamento escolhida no checkout (RF-VEN-006, RF-VEN-007, DEC-11, DEC-16): o endereço de entrega, que define
 * o frete, os cupons e os cartões. Nada disso fica gravado no checkout.
 *
 * @param cuponsPromocionais códigos digitados; no máximo um é aceito (RN-VEN-003)
 * @param cuponsDeTroca      ids dos cupons de troca do cliente, na ordem em que cobrem o total
 */
public record PagamentoInformado(
        EscolhaDeEndereco entrega,
        List<String> cuponsPromocionais,
        List<Long> cuponsDeTroca,
        List<CartaoInformado> cartoes) {

    public PagamentoInformado {
        cuponsPromocionais = cuponsPromocionais == null ? List.of() : List.copyOf(cuponsPromocionais);
        cuponsDeTroca = cuponsDeTroca == null ? List.of() : List.copyOf(cuponsDeTroca);
        cartoes = cartoes == null ? List.of() : List.copyOf(cartoes);
    }
}
