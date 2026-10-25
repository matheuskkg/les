package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

/**
 * Tentativa de chamada a um provedor, registrada em {@code chamada_ia} (RNF-REC-001, DEC-03). É um log só de
 * inserção: guarda o provedor, o modelo, a operação, o instante de início, a latência, a tentativa na cadeia, o
 * resultado e, quando o provedor informa, os tokens. O prompt e a resposta crus não são gravados.
 */
public record ChamadaIa(
        Long provedorIaId,
        String modelo,
        OperacaoIa operacao,
        LocalDateTime iniciadaEm,
        int latenciaMs,
        int tentativa,
        ResultadoDaChamada resultado,
        @Nullable Integer codigoHttp,
        @Nullable String erro,
        @Nullable Integer tokensEntrada,
        @Nullable Integer tokensSaida) {

    /** Limite da coluna {@code erro}, que não guarda a mensagem inteira de uma falha (DEC-03). */
    public static final int LIMITE_DO_ERRO = 500;

    public ChamadaIa {
        if (erro != null && erro.length() > LIMITE_DO_ERRO) {
            erro = erro.substring(0, LIMITE_DO_ERRO);
        }
    }
}
