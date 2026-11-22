package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.VALIDADE_VENCIDA;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.cartao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.relogio;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

/** DEC-06: o cartão não pode ser cadastrado com validade vencida no mês corrente. */
class ValidadeDoCartaoTest {

    private final ValidadesDosCartoes regra = new ValidadesDosCartoes(new ValidadeDoCartao(relogio()));

    @Test
    void dec06_validadeFuturaNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void dec06_validadeDoMesCorrenteEhAceita() {
        assertThat(violacoes(comValidade(YearMonth.of(2026, 10)), regra)).isEmpty();
    }

    @Test
    void dec06_validadeVencidaViolaARegra() {
        assertThat(violacoes(comValidade(VALIDADE_VENCIDA), regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RN-CLI-004");
    }

    @Test
    void dec06_validadeMesAnteriorAoCorrenteViolaARegra() {
        assertThat(violacoes(comValidade(YearMonth.of(2026, 9)), regra)).hasSize(1);
    }

    private static br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente comValidade(YearMonth validade) {
        var padrao = padrao();
        var novo =
                new br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao(
                        cartao().numero(), cartao().nomeImpresso(), validade);
        return new br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente(
                padrao.nome(),
                padrao.genero(),
                padrao.dataNascimento(),
                padrao.cpf(),
                padrao.email(),
                padrao.tipoTelefone(),
                padrao.ddd(),
                padrao.numero(),
                padrao.senha(),
                padrao.confirmacaoSenha(),
                padrao.enderecos(),
                List.of(novo));
    }
}
