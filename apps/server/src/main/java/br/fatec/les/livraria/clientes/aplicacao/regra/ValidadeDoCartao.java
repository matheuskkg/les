package br.fatec.les.livraria.clientes.aplicacao.regra;

import java.time.YearMonth;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Validade do cartão (DEC-06, que estende a RN-CLI-004): o cartão vale até o último dia do mês informado e não pode
 * ser cadastrado nem alterado já vencido no mês corrente. Valida um cartão; a lista do cadastro é percorrida por
 * {@code ValidadesDosCartoes}.
 */
@Component
@RequiredArgsConstructor
public class ValidadeDoCartao implements Regra<NovoCartao> {

    private final Relogio relogio;

    @Override
    public void validar(NovoCartao cartao, Violacoes violacoes) {
        YearMonth mesAtual = YearMonth.from(relogio.hoje());
        if (cartao != null && cartao.validade() != null && cartao.validade().isBefore(mesAtual)) {
            violacoes.adicionar("RN-CLI-004", "A validade não pode estar vencida.");
        }
    }
}
