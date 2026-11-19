package br.fatec.les.livraria.clientes.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Composição do cartão de crédito (RN-CLI-004, DEC-06, DEC-18): número, nome impresso e validade. A bandeira é
 * identificada pelo número e não é informada pelo cliente. O código de segurança não é gravado (DEC-10), então não é
 * exigido no cadastro nem aceito pela API. Valida um cartão; a lista do cadastro é percorrida por
 * {@code CartoesCompletos}.
 */
@Component
public class CartaoCompleto implements Regra<NovoCartao> {

    private static final String CODIGO = "RN-CLI-004";

    @Override
    public void validar(NovoCartao cartao, Violacoes violacoes) {
        if (cartao == null) {
            violacoes.adicionar(CODIGO, "Informe os dados do cartão.");
            return;
        }
        if (!numeroValido(cartao.numero())) {
            violacoes.adicionar(CODIGO, "Informe um número de cartão de 13 a 19 dígitos.");
        }
        if (cartao.nomeImpresso() == null || cartao.nomeImpresso().isBlank()) {
            violacoes.adicionar(CODIGO, "Informe o nome impresso no cartão.");
        }
        if (cartao.validade() == null) {
            violacoes.adicionar(CODIGO, "Informe a validade do cartão.");
        }
    }

    static boolean numeroValido(String numero) {
        if (numero == null) {
            return false;
        }
        int digitos = Telefone.somenteDigitos(numero).length();
        return digitos >= 13 && digitos <= 19;
    }
}
