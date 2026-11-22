package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import br.fatec.les.livraria.recomendacao.dominio.calculo.TokensDasVoltas;
import br.fatec.les.livraria.recomendacao.dominio.modelo.VoltaDaChamada;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Resposta estruturada de um provedor de IA (RNF-REC-001, DEC-03), com o conteúdo já validado pelo adapter e os tokens
 * informados pelo provedor, quando houver. O conteúdo é o modelo de domínio da operação, nunca texto livre.
 *
 * @param conteudo resposta da operação
 * @param tokensEntrada soma dos tokens de entrada das voltas, ou {@code null} se nenhuma os informou
 * @param tokensSaida soma dos tokens de saída das voltas, ou {@code null} se nenhuma os informou
 * @param voltas chamadas HTTP feitas ao provedor nesta mensagem, na ordem em que ocorreram
 */
public record RespostaDoProvedor<T>(
        T conteudo, @Nullable Integer tokensEntrada, @Nullable Integer tokensSaida, List<VoltaDaChamada> voltas) {

    public RespostaDoProvedor {
        Objects.requireNonNull(conteudo, "conteudo");
        voltas = voltas == null ? List.of() : List.copyOf(voltas);
    }

    /** Resposta sem a contagem de tokens e sem chamadas, como a do provedor fake (DEC-03). */
    public static <T> RespostaDoProvedor<T> semTokens(T conteudo) {
        return new RespostaDoProvedor<>(conteudo, null, null, List.of());
    }

    /** Resposta com as voltas informadas e os tokens somados a partir delas (DEC-03). */
    public static <T> RespostaDoProvedor<T> comVoltas(T conteudo, List<VoltaDaChamada> voltas) {
        TokensDasVoltas.Resumo tokens = TokensDasVoltas.calcular(voltas);
        return new RespostaDoProvedor<>(conteudo, tokens.entrada(), tokens.saida(), voltas);
    }

    /** Quantidade de chamadas HTTP feitas ao provedor nesta mensagem (DEC-03). */
    public int chamadasHttp() {
        return voltas.size();
    }
}
