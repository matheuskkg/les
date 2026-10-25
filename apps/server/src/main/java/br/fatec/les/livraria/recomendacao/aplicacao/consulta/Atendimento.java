package br.fatec.les.livraria.recomendacao.aplicacao.consulta;

/**
 * Resultado de um atendimento da cadeia de IA (RNF-REC-001, DEC-03): a resposta, o id da linha de
 * {@code chamada_ia} que a gerou e o nome do provedor que atendeu. O REC-03 usa o id para ligar a mensagem à
 * chamada.
 *
 * @param resposta    resposta da operação
 * @param chamadaIaId id da chamada bem-sucedida
 * @param provedor    nome do provedor que atendeu
 */
public record Atendimento<T>(
        T resposta,
        Long chamadaIaId,
        String provedor) {
}
