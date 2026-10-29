package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.recomendacao.dominio.calculo.TokensDasVoltas;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.VoltaDaChamada;
import lombok.Getter;

/**
 * Falha de uma chamada a um provedor de IA (RNF-REC-001, DEC-03). Carrega o resultado que vai para
 * {@code chamada_ia.resultado}, o código HTTP quando houver, a mensagem, que é truncada na gravação, e as voltas
 * HTTP já feitas na mensagem, inclusive a que falhou, para o log guardar {@code chamadas_http} e
 * {@code tokens_das_voltas}.
 */
@Getter
public class FalhaNaChamadaDeIa extends RuntimeException {

    private final ResultadoDaChamada resultado;
    private final Integer codigoHttp;
    private final List<VoltaDaChamada> voltas;
    private final int chamadasHttp;
    private final Integer tokensEntrada;
    private final Integer tokensSaida;

    public FalhaNaChamadaDeIa(ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem) {
        this(resultado, codigoHttp, mensagem, List.of());
    }

    public FalhaNaChamadaDeIa(ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem,
            Throwable causa) {
        this(resultado, codigoHttp, mensagem, List.of(), causa);
    }

    public FalhaNaChamadaDeIa(ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem,
            List<VoltaDaChamada> voltas) {
        this(resultado, codigoHttp, mensagem, voltas, null);
    }

    public FalhaNaChamadaDeIa(ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem,
            List<VoltaDaChamada> voltas, @Nullable Throwable causa) {
        super(mensagem, causa);
        this.resultado = Objects.requireNonNull(resultado, "resultado");
        this.codigoHttp = codigoHttp;
        this.voltas = List.copyOf(voltas);
        TokensDasVoltas.Resumo tokens = TokensDasVoltas.calcular(this.voltas);
        this.chamadasHttp = this.voltas.size();
        this.tokensEntrada = tokens.entrada();
        this.tokensSaida = tokens.saida();
    }
}
