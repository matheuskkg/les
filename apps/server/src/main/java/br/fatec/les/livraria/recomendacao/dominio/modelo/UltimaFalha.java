package br.fatec.les.livraria.recomendacao.dominio.modelo;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * Última tentativa sem sucesso de um provedor (RNF-REC-001, DEC-03). A mensagem de erro não é exposta, porque pode
 * conter o endereço do provedor, que nunca sai da configuração.
 *
 * @param em início da tentativa
 * @param resultado resultado da tentativa, diferente de {@link ResultadoDaChamada#SUCESSO}
 * @param codigoHttp código HTTP, quando houve resposta
 */
public record UltimaFalha(LocalDateTime em, ResultadoDaChamada resultado, @Nullable Integer codigoHttp) {

    public static UltimaFalha de(ChamadaIa chamada) {
        return new UltimaFalha(chamada.iniciadaEm(), chamada.resultado(), chamada.codigoHttp());
    }
}
