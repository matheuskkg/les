package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import lombok.Getter;

/**
 * Falha de uma chamada a um provedor de IA (RNF-REC-001, DEC-03). Carrega o resultado que vai para
 * {@code chamada_ia.resultado}, o código HTTP quando houver e a mensagem, que é truncada na gravação.
 */
@Getter
public class FalhaNaChamadaDeIa extends RuntimeException {

    private final ResultadoDaChamada resultado;
    private final Integer codigoHttp;

    public FalhaNaChamadaDeIa(ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem) {
        super(mensagem);
        this.resultado = Objects.requireNonNull(resultado, "resultado");
        this.codigoHttp = codigoHttp;
    }

    public FalhaNaChamadaDeIa(ResultadoDaChamada resultado, @Nullable Integer codigoHttp, String mensagem,
            Throwable causa) {
        super(mensagem, causa);
        this.resultado = Objects.requireNonNull(resultado, "resultado");
        this.codigoHttp = codigoHttp;
    }
}
