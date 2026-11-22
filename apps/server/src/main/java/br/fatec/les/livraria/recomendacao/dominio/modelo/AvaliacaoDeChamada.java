package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Avaliação do cliente a um item recomendado ou a uma resposta do assistente, atribuída ao provedor da chamada que
 * gerou o conteúdo (RNF-REC-001, DEC-03). Entra na taxa de aprovação das métricas.
 *
 * @param provedorIaId provedor da chamada que gerou a recomendação ou a resposta
 * @param avaliacao última avaliação dada
 */
public record AvaliacaoDeChamada(Long provedorIaId, Avaliacao avaliacao) {}
