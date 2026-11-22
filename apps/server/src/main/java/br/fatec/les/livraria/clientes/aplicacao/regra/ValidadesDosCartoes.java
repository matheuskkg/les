package br.fatec.les.livraria.clientes.aplicacao.regra;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validades dos cartões do cadastro (DEC-06, RN-CLI-004): percorre a lista e aplica {@link ValidadeDoCartao} a cada
 * item, numerando as mensagens. A regra de um único cartão é reutilizada na adição e na alteração (RF-CLI-007).
 */
@Component
@RequiredArgsConstructor
public class ValidadesDosCartoes implements Regra<NovoCliente> {

    private final ValidadeDoCartao validadeDoCartao;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            NovoCartao cartao = cartoes.get(i);
            if (cartao == null) {
                continue;
            }
            Violacoes doCartao = new Violacoes();
            validadeDoCartao.validar(cartao, doCartao);
            for (Violacao violacao : doCartao.lista()) {
                violacoes.adicionar(
                        violacao.codigo(), "Cartão " + (i + 1) + ": " + minusculaInicial(violacao.mensagem()));
            }
        }
    }

    private static String minusculaInicial(String mensagem) {
        return Character.toLowerCase(mensagem.charAt(0)) + mensagem.substring(1);
    }
}
