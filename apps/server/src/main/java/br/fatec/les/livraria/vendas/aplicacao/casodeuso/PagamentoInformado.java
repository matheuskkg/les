package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;

import java.util.List;
import java.util.Objects;

/**
 * Forma de pagamento escolhida no checkout (RF-VEN-006, RF-VEN-007, DEC-11, DEC-16): o endereço de entrega, que define
 * o frete, os cupons e os cartões. Nada disso fica gravado no checkout.
 *
 * @param cuponsPromocionais códigos digitados; no máximo um é aceito (RN-VEN-003)
 * @param cuponsDeTroca ids dos cupons de troca do cliente, na ordem em que cobrem o total
 */
public record PagamentoInformado(
        EscolhaDeEndereco entrega,
        List<String> cuponsPromocionais,
        List<Long> cuponsDeTroca,
        List<CartaoInformado> cartoes) {

    public PagamentoInformado {
        cuponsPromocionais = semVazios(cuponsPromocionais);
        cuponsDeTroca = semVazios(cuponsDeTroca);
        cartoes = semVazios(cartoes);
    }

    /** Lista ausente vale como vazia; item vazio na lista é erro de quem enviou, não do servidor. */
    private static <T> List<T> semVazios(List<T> itens) {
        if (itens == null) {
            return List.of();
        }
        if (itens.stream().anyMatch(Objects::isNull)) {
            throw new ExcecaoDeNegocio("RF-VEN-007", "A forma de pagamento tem um cupom ou cartão vazio.");
        }
        return List.copyOf(itens);
    }
}
