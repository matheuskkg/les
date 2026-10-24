package br.fatec.les.livraria.clientes.dominio.regra;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Composição do cartão de crédito (RN-CLI-004, DEC-06): número, nome impresso, bandeira e validade. O código de
 * segurança não é gravado (DEC-10), então não é exigido no cadastro.
 */
@Component
public class CartaoCompleto implements Regra<NovoCliente> {

    private static final String CODIGO = "RN-CLI-004";

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            NovoCartao cartao = cartoes.get(i);
            String referencia = "Cartão " + (i + 1) + ": ";
            if (cartao == null) {
                violacoes.adicionar(CODIGO, referencia + "informe os dados do cartão.");
                continue;
            }
            if (!numeroValido(cartao.numero())) {
                violacoes.adicionar(CODIGO, referencia + "informe um número de cartão de 13 a 19 dígitos.");
            }
            if (cartao.nomeImpresso() == null || cartao.nomeImpresso().isBlank()) {
                violacoes.adicionar(CODIGO, referencia + "informe o nome impresso no cartão.");
            }
            if (cartao.bandeiraId() == null) {
                violacoes.adicionar(CODIGO, referencia + "informe a bandeira do cartão.");
            }
            if (cartao.validade() == null) {
                violacoes.adicionar(CODIGO, referencia + "informe a validade do cartão.");
            }
        }
    }

    static boolean numeroValido(String numero) {
        if (numero == null) {
            return false;
        }
        int digitos = DadosObrigatoriosCliente.somenteDigitos(numero).length();
        return digitos >= 13 && digitos <= 19;
    }
}
