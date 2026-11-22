package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.endereco;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;

import org.junit.jupiter.api.Test;

import java.util.List;

/** RN-CLI-003: composição do endereço e as adaptações do DEC-10 (país fixo e CEP de 8 dígitos). */
class EnderecoCompletoTest {

    private final EnderecosCompletos regra = new EnderecosCompletos(new EnderecoCompleto());

    @Test
    void rnCli003_enderecoCompletoNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli003_camposObrigatoriosFaltando() {
        NovoEndereco incompleto = new NovoEndereco("", null, null, " ", null, "", "123", " ", null, null);
        NovoCliente alvo = comEndereco(incompleto);

        assertThat(violacoes(alvo, regra)).isNotEmpty().allMatch(violacao -> violacao.codigo().equals("RN-CLI-003"));
    }

    @Test
    void rnCli003_cepComMascaraEhAceito() {
        NovoEndereco comMascara =
                new NovoEndereco(
                        "Casa",
                        endereco().tipoEndereco(),
                        endereco().tipoLogradouro(),
                        "Rua das Flores",
                        "123",
                        "Jardim Paulista",
                        "01415-000",
                        "São Paulo",
                        endereco().uf(),
                        null);

        assertThat(violacoes(comEndereco(comMascara), regra)).isEmpty();
    }

    @Test
    void rnCli003_cepForaDeOitoDigitosEhRejeitado() {
        NovoEndereco cepInvalido =
                new NovoEndereco(
                        "Casa",
                        endereco().tipoEndereco(),
                        endereco().tipoLogradouro(),
                        "Rua das Flores",
                        "123",
                        "Jardim Paulista",
                        "123",
                        "São Paulo",
                        endereco().uf(),
                        null);

        assertThat(violacoes(comEndereco(cepInvalido), regra))
                .anyMatch(violacao -> violacao.mensagem().contains("CEP"));
    }

    private static NovoCliente comEndereco(NovoEndereco endereco) {
        NovoCliente padrao = padrao();
        return new NovoCliente(
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
                List.of(endereco),
                padrao.cartoes());
    }
}
