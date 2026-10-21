package br.fatec.les.livraria.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ViolacoesTest {

    private final Regra<String> naoVazio = (texto, v) -> {
        if (texto.isBlank()) {
            v.adicionar("RN-TESTE-001", "Não pode ser vazio.");
        }
    };
    private final Regra<String> curto = (texto, v) -> {
        if (texto.length() < 3) {
            v.adicionar("RN-TESTE-002", "Mínimo de 3 caracteres.");
        }
    };

    @Test
    void semViolacoesNaoLanca() {
        assertThatCode(() -> Violacoes.verificar("abcd", naoVazio, curto)).doesNotThrowAnyException();
    }

    @Test
    void executaTodasAsRegrasEAcumulaAsViolacoes() {
        assertThatThrownBy(() -> Violacoes.verificar(" ", naoVazio, curto))
                .isInstanceOf(ExcecaoDeNegocio.class)
                .satisfies(e -> assertThat(((ExcecaoDeNegocio) e).getViolacoes())
                        .extracting(Violacao::codigo)
                        .containsExactly("RN-TESTE-001", "RN-TESTE-002"));
    }

    @Test
    void aceitaRegraDeSupertipo() {
        Regra<CharSequence> regra = (texto, v) -> v.adicionar("RN-TESTE-003", "Sempre falha.");

        assertThatThrownBy(() -> Violacoes.verificar("abc", regra)).isInstanceOf(ExcecaoDeNegocio.class);
    }
}
