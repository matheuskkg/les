package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;

/**
 * Log das tentativas de chamada à IA (RNF-REC-001, DEC-03). Cada tentativa, com sucesso ou falha, vira uma linha em
 * {@code chamada_ia}; a gravação usa transação própria para sobreviver ao rollback de quem chamou.
 */
public interface ChamadasIaRepositorio {

    /** Registra a tentativa e devolve o id gerado. */
    Long registrar(ChamadaIa chamada);
}
