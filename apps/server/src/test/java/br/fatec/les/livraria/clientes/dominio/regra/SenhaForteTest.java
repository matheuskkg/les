package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.comSenha;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** RNF-CLI-001: senha forte (mínimo, maiúscula, minúscula, especial e limite do BCrypt). */
class SenhaForteTest {

    private final SenhaForte regra = new SenhaForte();

    @Test
    void rnfCli001_senhaForteNaoTemViolacao() {
        assertThat(violacoes(comSenha("Senha@123"), regra)).isEmpty();
    }

    @Test
    void rnfCli001_senhaSemMaiuscula() {
        assertThat(violacoes(comSenha("senha@123"), regra))
                .extracting(violacao -> violacao.codigo())
                .contains("RNF-CLI-001");
        assertThat(violacoes(comSenha("senha@123"), regra))
                .anyMatch(violacao -> violacao.mensagem().contains("maiúscula"));
    }

    @Test
    void rnfCli001_senhaSemMinuscula() {
        assertThat(violacoes(comSenha("SENHA@123"), regra))
                .anyMatch(violacao -> violacao.mensagem().contains("minúscula"));
    }

    @Test
    void rnfCli001_senhaSemCaractereEspecial() {
        assertThat(violacoes(comSenha("Senha1234"), regra))
                .anyMatch(violacao -> violacao.mensagem().contains("especial"));
    }

    @Test
    void rnfCli001_senhaComMenosDeOitoCaracteres() {
        assertThat(violacoes(comSenha("Ab@1"), regra))
                .anyMatch(violacao -> violacao.mensagem().contains("8 caracteres"));
    }

    @Test
    void rnfCli001_senhaAcimaDoLimiteDeBytesDoBcrypt() {
        String senha = "Ab@" + "a".repeat(70);

        assertThat(senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isGreaterThan(72);
        assertThat(violacoes(comSenha(senha), regra))
                .anyMatch(violacao -> violacao.mensagem().contains("72 bytes"));
    }

    @Test
    void rnfCli001_senhaEmBranco() {
        assertThat(violacoes(comSenha("   "), regra))
                .anyMatch(violacao -> violacao.mensagem().contains("Informe a senha"));
    }
}
