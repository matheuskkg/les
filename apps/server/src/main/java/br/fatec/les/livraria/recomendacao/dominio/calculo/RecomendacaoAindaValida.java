package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.Recomendacao;

/**
 * A recomendação da vitrine é reaproveitada até a próxima compra do cliente ou por 24 horas (RNF-REC-001, DEC-03).
 * A compra é contada pelos pedidos que contam como venda, e não pela data do pedido: um pedido realizado antes da
 * geração e aprovado depois também a invalida. A gravada sem IA vale só 1 hora, para a IA ser tentada de novo logo
 * que o provedor voltar.
 */
@Component
public class RecomendacaoAindaValida {

    public static final Duration VALIDADE = Duration.ofHours(24);
    public static final Duration VALIDADE_SEM_IA = Duration.ofHours(1);

    /**
     * @param recomendacao        a mais recente do cliente
     * @param agora               instante atual, pelo relógio da aplicação
     * @param comprasDoCliente    pedidos do cliente que contam como venda agora
     */
    public boolean valida(Recomendacao recomendacao, LocalDateTime agora, int comprasDoCliente) {
        Duration validade = recomendacao.semIa() ? VALIDADE_SEM_IA : VALIDADE;
        boolean dentroDoPrazo = agora.isBefore(recomendacao.geradaEm().plus(validade));
        return dentroDoPrazo && recomendacao.comprasConsideradas() == comprasDoCliente;
    }
}
