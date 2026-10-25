package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Provedor na lista enviada pelo ADMIN para alterar a cadeia (RNF-REC-001, DEC-03). A ordem é a posição na lista.
 *
 * @param id    id do provedor em {@code provedor_ia}
 * @param ativo se o provedor entra na cadeia
 */
public record PosicaoNaCadeia(Long id, boolean ativo) {
}
