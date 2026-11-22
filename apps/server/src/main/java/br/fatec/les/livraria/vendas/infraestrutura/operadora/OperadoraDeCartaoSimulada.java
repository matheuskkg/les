package br.fatec.les.livraria.vendas.infraestrutura.operadora;

import br.fatec.les.livraria.vendas.aplicacao.porta.OperadoraDeCartao;
import br.fatec.les.livraria.vendas.dominio.pagamento.CobrancaNoCartao;
import br.fatec.les.livraria.vendas.dominio.pagamento.RespostaDaOperadora;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Operadora simulada (RN-VEN-007, DEC-05.2), com as condições avaliadas nesta ordem: cartão vencido na data da
 * transação (DEC-06), número terminado em {@code 0000} e código de segurança {@code 000}. Qualquer outro caso é
 * aprovado.
 */
@Component
public class OperadoraDeCartaoSimulada implements OperadoraDeCartao {

    static final String VENCIDO = "Cartão vencido";
    static final String RECUSADA = "Transação recusada pela operadora";
    static final String CODIGO_INVALIDO = "Código de segurança inválido";

    @Override
    public RespostaDaOperadora cobrar(CobrancaNoCartao cobranca, LocalDate data) {
        if (cobranca.validade().isBefore(YearMonth.from(data))) {
            return RespostaDaOperadora.recusar(VENCIDO);
        }
        if (cobranca.numero().endsWith("0000")) {
            return RespostaDaOperadora.recusar(RECUSADA);
        }
        if (cobranca.codigoSeguranca().equals("000")) {
            return RespostaDaOperadora.recusar(CODIGO_INVALIDO);
        }
        return RespostaDaOperadora.aprovar();
    }
}
