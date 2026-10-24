package br.fatec.les.livraria.acesso.dominio.modelo;

/**
 * Situação da conta derivada do usuário e do relógio (RF-CLI-003, DEC-10, DEC-15.4). Nada é gravado quando o prazo de
 * reativação vence: a conta só deixa de estar em uso na consulta.
 */
public enum SituacaoConta {
    ATIVA,
    INATIVA,
    ENCERRADA
}
