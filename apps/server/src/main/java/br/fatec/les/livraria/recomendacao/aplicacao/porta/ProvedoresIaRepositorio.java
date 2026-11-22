package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;

import java.util.List;

/**
 * Provedores da cadeia de IA (RNF-REC-001, DEC-03). A ordem e a ativação ficam na tabela {@code provedor_ia}; o REC-05
 * altera a lista pelo ADMIN e invalida o cache da cadeia.
 */
public interface ProvedoresIaRepositorio {

    /** Provedores na ordem de preferência da cadeia, inclusive os inativos. */
    List<ProvedorIa> listarEmOrdem();

    /** Grava a lista, atualizando as linhas existentes e criando as que ainda não têm id. */
    List<ProvedorIa> salvar(List<ProvedorIa> provedores);
}
