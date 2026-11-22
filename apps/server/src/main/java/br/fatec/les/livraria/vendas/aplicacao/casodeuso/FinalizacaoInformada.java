package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * O que o cliente envia em "Finalizar compra" (RF-VEN-005, RF-VEN-006, RF-VEN-007, RF-VEN-008, DEC-10, DEC-11): a forma
 * de pagamento, o endereço de cobrança e o código de segurança de cada cartão, na ordem dos cartões. O código só segue
 * para a operadora; não é gravado.
 *
 * @param codigosDeSeguranca um por cartão de {@code pagamento.cartoes()}; um código ausente vale como inválido
 */
public record FinalizacaoInformada(
        PagamentoInformado pagamento, EscolhaDeEndereco cobranca, List<String> codigosDeSeguranca) {

    public FinalizacaoInformada {
        Objects.requireNonNull(pagamento, "pagamento");
        codigosDeSeguranca =
                codigosDeSeguranca == null
                        ? List.of()
                        : Collections.unmodifiableList(new ArrayList<>(codigosDeSeguranca));
        if (codigosDeSeguranca.size() != pagamento.cartoes().size()) {
            throw new ExcecaoDeNegocio("RN-CLI-004", "Informe o código de segurança de cada cartão.");
        }
    }

    @Override
    public String toString() {
        return "FinalizacaoInformada[" + pagamento + ", " + cobranca + "]";
    }
}
