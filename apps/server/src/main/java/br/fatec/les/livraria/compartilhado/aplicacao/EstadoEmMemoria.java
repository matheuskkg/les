package br.fatec.les.livraria.compartilhado.aplicacao;

/**
 * Estado guardado só em memória por um módulo, que o reset dos testes E2E precisa limpar (DEC-09). O
 * {@code compartilhado} não conhece os módulos: quem tem estado implementa esta interface, e o controller de apoio
 * recebe a lista de implementações.
 */
public interface EstadoEmMemoria {

    /** Descarta o estado em memória, como caches e esperas. */
    void limpar();
}
