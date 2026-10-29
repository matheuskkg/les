package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

/**
 * Tentativa de chamada a um provedor, registrada em {@code chamada_ia} (RNF-REC-001, DEC-03). É um log só de
 * inserção: guarda o provedor, o modelo, a operação, o instante de início, a latência, a tentativa na cadeia, o
 * resultado, quantas chamadas HTTP a mensagem fez, os tokens de cada volta e os tokens somados. O prompt e a
 * resposta crus não são gravados.
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
        @Nullable Integer tokensSaida,
        int chamadasHttp,
        @Nullable String tokensDasVoltas) {

    /** Limite da coluna {@code erro}, que não guarda a mensagem inteira de uma falha (DEC-03). */
    public static final int LIMITE_DO_ERRO = 500;

    /** Limite da coluna {@code tokens_das_voltas} (DEC-03). */
    public static final int LIMITE_DOS_TOKENS_DAS_VOLTAS = 200;

    public ChamadaIa {
        if (erro != null && erro.length() > LIMITE_DO_ERRO) {
            erro = erro.substring(0, LIMITE_DO_ERRO);
        }
        tokensDasVoltas = truncarTokensDasVoltas(tokensDasVoltas);
    }

    /**
     * Corta a descrição em 200 caracteres sem partir uma volta: quando o texto passa do limite, mantém só as voltas
     * completas até o último {@code ;} que cabe. Se nem uma volta completa cabe, a descrição fica nula.
     */
    private static @Nullable String truncarTokensDasVoltas(@Nullable String tokensDasVoltas) {
        if (tokensDasVoltas == null || tokensDasVoltas.length() <= LIMITE_DOS_TOKENS_DAS_VOLTAS) {
            return tokensDasVoltas;
        }
        int corte = tokensDasVoltas.lastIndexOf(';', LIMITE_DOS_TOKENS_DAS_VOLTAS - 1);
        return corte > 0 ? tokensDasVoltas.substring(0, corte) : null;
    }
}
