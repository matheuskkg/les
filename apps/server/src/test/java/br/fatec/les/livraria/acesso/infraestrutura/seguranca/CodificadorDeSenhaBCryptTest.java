package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Conferência de senhas pelo BCrypt, inclusive o limite de 72 bytes (RNF-CLI-003). */
class CodificadorDeSenhaBCryptTest {

    private final CodificadorDeSenhaBCrypt senhas = new CodificadorDeSenhaBCrypt();

    @Test
    void confereSenhaCorreta() {
        // Hash de custo baixo só para o teste ser rápido; o adapter confere qualquer custo.
        String hash = new BCryptPasswordEncoder(4).encode("Senha@123");

        assertThat(senhas.confere("Senha@123", hash)).isTrue();
        assertThat(senhas.confere("outra-senha", hash)).isFalse();
    }

    @Test
    void senhaAcimaDe72BytesNaoConfereEmVezDeLancar() {
        String hash = new BCryptPasswordEncoder(4).encode("Senha@123");

        assertThat(senhas.confere("a".repeat(73), hash)).isFalse();
    }

    @Test
    void senhaOuHashNuloNaoConfere() {
        assertThat(senhas.confere(null, "hash")).isFalse();
        assertThat(senhas.confere("Senha@123", null)).isFalse();
    }
}
