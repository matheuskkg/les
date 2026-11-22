package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import br.fatec.les.livraria.recomendacao.dominio.calculo.TokensDasVoltas;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.VoltaDaChamada;

import lombok.Getter;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Falha de uma chamada a um provedor de IA (RNF-REC-001, DEC-03). Carrega o resultado que vai para {@code
 * chamada_ia.resultado}, o código HTTP quando houver, a mensagem, que é truncada na gravação, e as voltas HTTP já
 * feitas na mensagem, inclusive a que falhou, para o log guardar {@code chamadas_http} e {@code tokens_das_voltas}. No
 * 429, leva também a espera sugerida pelo provedor e se a cota esgotada é diária (REC-09).
 */
@Getter
public class FalhaNaChamadaDeIa extends RuntimeException {

    private final ResultadoDaChamada resultado;
    private final Integer codigoHttp;
    private final List<VoltaDaChamada> voltas;
    private final int chamadasHttp;
    private final Integer tokensEntrada;
    private final Integer tokensSaida;
    private final @Nullable Duration esperaSugerida;
    private final boolean cotaDiaria;

    public FalhaNaChamadaDeIa(ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem) {
        this(resultado, codigoHttp, mensagem, List.of(), null, null, false);
    }

    public FalhaNaChamadaDeIa(
            ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem, Throwable causa) {
        this(resultado, codigoHttp, mensagem, List.of(), causa, null, false);
    }

    public FalhaNaChamadaDeIa(
            ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem, List<VoltaDaChamada> voltas) {
        this(resultado, codigoHttp, mensagem, voltas, null, null, false);
    }

    public FalhaNaChamadaDeIa(
            ResultadoDaChamada resultado,
            @Nullable Integer codigoHttp,
            String mensagem,
            List<VoltaDaChamada> voltas,
            @Nullable Throwable causa) {
        this(resultado, codigoHttp, mensagem, voltas, causa, null, false);
    }

    public FalhaNaChamadaDeIa(
            ResultadoDaChamada resultado,
            @Nullable Integer codigoHttp,
            String mensagem,
            List<VoltaDaChamada> voltas,
            @Nullable Throwable causa,
            @Nullable Duration esperaSugerida,
            boolean cotaDiaria) {
        super(mensagem, causa);
        this.resultado = Objects.requireNonNull(resultado, "resultado");
        this.codigoHttp = codigoHttp;
        this.voltas = List.copyOf(voltas);
        TokensDasVoltas.Resumo tokens = TokensDasVoltas.calcular(this.voltas);
        this.chamadasHttp = this.voltas.size();
        this.tokensEntrada = tokens.entrada();
        this.tokensSaida = tokens.saida();
        this.esperaSugerida = esperaSugerida;
        this.cotaDiaria = cotaDiaria;
    }
}
