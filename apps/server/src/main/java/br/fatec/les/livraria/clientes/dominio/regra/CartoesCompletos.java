package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cartões do cadastro do cliente (RN-CLI-004): percorre a lista e aplica {@link CartaoCompleto} a cada item, numerando
 * as mensagens. A validação de um único cartão é reutilizada pelos casos de uso de adição e alteração (RF-CLI-007).
 */
@Component
@RequiredArgsConstructor
public class CartoesCompletos implements Regra<NovoCliente> {

    private final CartaoCompleto cartaoCompleto;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            String referencia = "Cartão " + (i + 1) + ": ";
            NovoCartao cartao = cartoes.get(i);
            if (cartao == null) {
                violacoes.adicionar("RN-CLI-004", referencia + "informe os dados do cartão.");
                continue;
            }
            Violacoes doCartao = new Violacoes();
            cartaoCompleto.validar(cartao, doCartao);
            for (Violacao violacao : doCartao.lista()) {
                violacoes.adicionar(violacao.codigo(), referencia + minusculaInicial(violacao.mensagem()));
            }
        }
    }

    private static String minusculaInicial(String mensagem) {
        return Character.toLowerCase(mensagem.charAt(0)) + mensagem.substring(1);
    }
}
