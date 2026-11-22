package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;

import org.junit.jupiter.api.Test;

/** RN-CLI-006: CPF válido, conferido pelo value object {@code Cpf}. */
class CpfValidoTest {

    private final CpfValido regra = new CpfValido();

    @Test
    void rnCli006_cpfValidoNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli006_cpfComDigitoVerificadorErrado() {
        assertThat(violacoes(comCpf("52998224724"), regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RN-CLI-006");
    }

    @Test
    void rnCli006_cpfComTodosOsDigitosIguais() {
        assertThat(violacoes(comCpf("11111111111"), regra)).isNotEmpty();
    }

    private static NovoCliente comCpf(String cpf) {
        NovoCliente padrao = padrao();
        return new NovoCliente(
                padrao.nome(),
                padrao.genero(),
                padrao.dataNascimento(),
                cpf,
                padrao.email(),
                padrao.tipoTelefone(),
                padrao.ddd(),
                padrao.numero(),
                padrao.senha(),
                padrao.confirmacaoSenha(),
                padrao.enderecos(),
                padrao.cartoes());
    }
}
