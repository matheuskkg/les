package br.fatec.les.livraria.clientes.aplicacao.regra;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Validade do cartão (DEC-06, que estende a RN-CLI-004): o cartão vale até o último dia do mês informado e não pode
 * ser cadastrado já vencido no mês corrente.
 */
@Component
@RequiredArgsConstructor
public class ValidadeDoCartao implements Regra<NovoCliente> {

    private final Relogio relogio;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        YearMonth mesAtual = YearMonth.from(relogio.hoje());
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            NovoCartao cartao = cartoes.get(i);
            if (cartao != null && cartao.validade() != null && cartao.validade().isBefore(mesAtual)) {
                violacoes.adicionar("RN-CLI-004",
                        "Cartão " + (i + 1) + ": a validade não pode estar vencida.");
            }
        }
    }
}
