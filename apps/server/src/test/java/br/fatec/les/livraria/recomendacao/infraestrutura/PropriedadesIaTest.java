package br.fatec.les.livraria.recomendacao.infraestrutura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

/** Validação e padrões das opções de cada provedor de IA (RNF-REC-001, DEC-03). */
class PropriedadesIaTest {

    private static final String URL = "http://127.0.0.1:1/v1";

    @Test
    void rnfRec001_opcoesAusentesUsamOsPadroes() {
        PropriedadesIa.Provedor provedor = new PropriedadesIa.Provedor(URL, "modelo", "chave", null, null, null, 5, 0);

        assertThat(provedor.toolChoice()).isEqualTo(PropriedadesIa.Provedor.TOOL_CHOICE_AUTO);
        assertThat(provedor.exigeToolChoice()).isFalse();
        assertThat(provedor.formato()).isEqualTo(PropriedadesIa.Provedor.FORMATO_FERRAMENTA);
        assertThat(provedor.usaFerramenta()).isTrue();
        assertThat(provedor.usaJsonSchema()).isFalse();
        assertThat(provedor.cabecalhos()).isEmpty();
        assertThat(provedor.maxChamadas()).isEqualTo(5);
        assertThat(provedor.maxTokens()).isZero();
    }

    @Test
    void rnfRec001_opcoesValidasSaoAceitasSemDiferenciarMaiusculas() {
        PropriedadesIa.Provedor provedor =
                new PropriedadesIa.Provedor(
                        URL, "modelo", "chave", "REQUIRED", "JSON-SCHEMA", Map.of("X-Title", "Livraria LES"), 3, 0);

        assertThat(provedor.exigeToolChoice()).isTrue();
        assertThat(provedor.usaFerramenta()).isFalse();
        assertThat(provedor.usaJsonSchema()).isTrue();
        assertThat(provedor.cabecalhos()).containsEntry("X-Title", "Livraria LES");
        assertThat(provedor.maxChamadas()).isEqualTo(3);
    }

    @Test
    void rnfRec001_formatosDeTextoNaoUsamAFerramentaDeResposta() {
        PropriedadesIa.Provedor instrucoes =
                new PropriedadesIa.Provedor(URL, "modelo", "chave", null, "INSTRUCOES", null, 5, 0);
        PropriedadesIa.Provedor jsonSchema =
                new PropriedadesIa.Provedor(URL, "modelo", "chave", null, "JSON-SCHEMA", null, 5, 0);

        assertThat(instrucoes.formato()).isEqualTo(PropriedadesIa.Provedor.FORMATO_INSTRUCOES);
        assertThat(instrucoes.usaFerramenta()).isFalse();
        assertThat(instrucoes.usaJsonSchema()).isFalse();
        assertThat(jsonSchema.usaFerramenta()).isFalse();
        assertThat(jsonSchema.usaJsonSchema()).isTrue();
    }

    @Test
    void rnfRec001_toolChoiceInvalidoEhRejeitado() {
        assertThatThrownBy(() -> new PropriedadesIa.Provedor(URL, "modelo", "chave", "sempre", null, null, 5, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("livraria.ia.provedores.*.tool-choice deve ser auto ou required.");
    }

    @Test
    void rnfRec001_formatoInvalidoEhRejeitado() {
        assertThatThrownBy(() -> new PropriedadesIa.Provedor(URL, "modelo", "chave", null, "xml", null, 5, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("livraria.ia.provedores.*.formato deve ser ferramenta, json-schema ou instrucoes.");
    }

    @Test
    void rnfRec001_maxChamadasMenorQueUmEhRejeitado() {
        assertThatThrownBy(() -> new PropriedadesIa.Provedor(URL, "modelo", "chave", null, null, null, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("livraria.ia.provedores.*.max-chamadas deve ser pelo menos 1.");
    }

    @Test
    void rnfRec001_maxTokensAusenteOuZeroNaoLimitaASaida() {
        PropriedadesIa.Provedor provedor = new PropriedadesIa.Provedor(URL, "modelo", "chave", null, null, null, 5, 0);

        assertThat(provedor.maxTokens()).isZero();
        assertThat(provedor.temTetoDeSaida()).isFalse();
    }

    @Test
    void rnfRec001_maxTokensValidoDefineOTetoDeSaida() {
        PropriedadesIa.Provedor provedor =
                new PropriedadesIa.Provedor(URL, "modelo", "chave", null, null, null, 5, 800);

        assertThat(provedor.maxTokens()).isEqualTo(800);
        assertThat(provedor.temTetoDeSaida()).isTrue();
    }

    @Test
    void rnfRec001_maxTokensNegativoEhRejeitado() {
        assertThatThrownBy(() -> new PropriedadesIa.Provedor(URL, "modelo", "chave", null, null, null, 5, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("livraria.ia.provedores.*.max-tokens deve ser 0 (sem teto) ou pelo menos 1.");
    }

    @Test
    void rnfRec001_urlBaseEModeloSaoObrigatorios() {
        assertThatThrownBy(() -> new PropriedadesIa.Provedor(" ", "modelo", "chave", null, null, null, 5, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("livraria.ia.provedores.*.url-base é obrigatório.");
        assertThatThrownBy(() -> new PropriedadesIa.Provedor(URL, "", "chave", null, null, null, 5, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("livraria.ia.provedores.*.modelo é obrigatório.");
    }

    @Test
    void rnfRec001_tempoLimitePrecisaSerPositivo() {
        assertThatThrownBy(() -> new PropriedadesIa(Duration.ZERO, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("livraria.ia.tempo-limite deve ser positivo.");
    }

    @Test
    void rnfRec001_chaveVaziaMantemOProvedorForaDaCadeia() {
        PropriedadesIa.Provedor provedor = new PropriedadesIa.Provedor(URL, "modelo", "", null, null, null, 5, 0);

        assertThat(provedor.temChave()).isFalse();
    }
}
