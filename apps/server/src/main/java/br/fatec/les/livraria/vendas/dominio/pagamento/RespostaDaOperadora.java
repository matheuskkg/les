package br.fatec.les.livraria.vendas.dominio.pagamento;

/**
 * Resposta da operadora para um cartão (RN-VEN-007, DEC-05.2).
 *
 * @param motivo por que o cartão foi recusado; nulo quando aprovado
 */
public record RespostaDaOperadora(boolean aprovada, String motivo) {

    public static RespostaDaOperadora aprovar() {
        return new RespostaDaOperadora(true, null);
    }

    public static RespostaDaOperadora recusar(String motivo) {
        return new RespostaDaOperadora(false, motivo);
    }
}
