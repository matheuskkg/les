package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.time.LocalDate;

/**
 * Ponto do gráfico de chamadas da IA (RNF-REC-001, DEC-03): todas as tentativas do dia, de todos os provedores, e as
 * que falharam.
 */
public record ChamadasNoDia(LocalDate dia, long chamadas, long falhas) {}
