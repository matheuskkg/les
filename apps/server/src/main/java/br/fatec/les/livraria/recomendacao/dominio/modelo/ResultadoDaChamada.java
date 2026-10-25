package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Resultado de uma tentativa de chamada a um provedor de IA (RNF-REC-001, DEC-03), gravado em {@code chamada_ia}.
 * {@link #SUCESSO} é o único que produz resposta; os demais fazem a cadeia passar ao próximo provedor.
 */
public enum ResultadoDaChamada {
    SUCESSO,
    COTA_ESGOTADA,
    ERRO_HTTP,
    TEMPO_ESGOTADO,
    ERRO_DE_REDE,
    RESPOSTA_INVALIDA
}
