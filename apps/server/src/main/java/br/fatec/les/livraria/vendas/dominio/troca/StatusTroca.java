package br.fatec.les.livraria.vendas.dominio.troca;

/**
 * Status da troca (RN-VEN-011, RN-VEN-012, DEC-12). {@code TROCA_RECUSADA} é extensão do DRS: o admin pode negar uma
 * troca indevida, com motivo.
 */
public enum StatusTroca {
    EM_TROCA,
    TROCA_AUTORIZADA,
    TROCA_RECUSADA,
    TROCADO
}
