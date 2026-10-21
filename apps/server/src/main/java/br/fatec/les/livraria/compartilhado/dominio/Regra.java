package br.fatec.les.livraria.compartilhado.dominio;

/**
 * Uma regra de negócio ou validação. Cada implementação fica em um arquivo próprio e informa no Javadoc o ID do
 * requisito que atende.
 *
 * @param <T> o que a regra valida: um modelo de domínio ou o comando de entrada de uma operação
 */
@FunctionalInterface
public interface Regra<T> {

    /** Registra em {@code violacoes} tudo o que estiver errado em {@code alvo}; não lança exceção. */
    void validar(T alvo, Violacoes violacoes);
}
