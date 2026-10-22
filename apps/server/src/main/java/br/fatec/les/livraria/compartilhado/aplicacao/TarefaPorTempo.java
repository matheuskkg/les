package br.fatec.les.livraria.compartilhado.aplicacao;

/**
 * Tarefa periódica cujo efeito depende do {@link br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio}. Além
 * do agendamento, o profile {@code e2e} a executa logo depois de mudar o relógio (DEC-09).
 */
public interface TarefaPorTempo {

    void executar();
}
