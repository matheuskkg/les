package br.fatec.les.livraria.vendas.dominio.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

/**
 * Carrinho de compras do cliente (RF-VEN-001, RF-VEN-002, DEC-12, DEC-16). Há um carrinho por cliente,
 * criado na primeira adição. O prazo de bloqueio ({@code expiraEm}) é contado a partir do último item
 * incluído (DEC-05.1) e as reservas de estoque acompanham esse prazo.
 *
 * <p>O carrinho não guarda preço: a leitura usa o preço de venda atual do catálogo. O pedido é que copia os
 * dados na finalização (DEC-12, DEC-16).</p>
 */
public class Carrinho {

    private final Long id;
    private final Long clienteId;
    private final List<ItemCarrinho> itens;
    private LocalDateTime expiraEm;
    private LocalDateTime avisoEnviadoEm;

    public Carrinho(Long id, Long clienteId, LocalDateTime expiraEm, LocalDateTime avisoEnviadoEm,
            List<ItemCarrinho> itens) {
        this.id = id;
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId");
        this.itens = new ArrayList<>(Objects.requireNonNull(itens, "itens"));
        this.expiraEm = expiraEm;
        this.avisoEnviadoEm = avisoEnviadoEm;
    }

    /** Carrinho ainda sem id e sem itens, para o primeiro item do cliente (criação preguiçosa). */
    public static Carrinho vazio(Long clienteId) {
        return new Carrinho(null, clienteId, null, null, List.of());
    }

    /** Quantidade já existente de um livro; zero quando ele não está no carrinho. */
    public int quantidadeDoLivro(Long livroId) {
        return item(livroId).map(ItemCarrinho::getQuantidade).orElse(0);
    }

    public Optional<ItemCarrinho> item(Long livroId) {
        Objects.requireNonNull(livroId, "livroId");
        return itens.stream().filter(item -> item.getLivroId().equals(livroId)).findFirst();
    }

    /**
     * Adiciona um livro ou soma à linha existente (RF-VEN-001). A linha é única por livro (DEC-16).
     *
     * @throws IllegalArgumentException se a quantidade não for positiva
     */
    public void adicionar(Long livroId, int quantidade) {
        Objects.requireNonNull(livroId, "livroId");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade adicionada deve ser no mínimo 1.");
        }
        item(livroId).ifPresentOrElse(item -> item.somar(quantidade),
                () -> itens.add(new ItemCarrinho(null, livroId, quantidade)));
    }

    /**
     * Define a quantidade de um item existente (RF-VEN-002). A remoção é feita por {@link #remover(Long)},
     * porque zero não é uma quantidade válida.
     *
     * @throws ExcecaoDeRecursoNaoEncontrado se o livro não estiver no carrinho
     */
    public void alterarQuantidade(Long livroId, int quantidade) {
        itemExigido(livroId).alterarQuantidade(quantidade);
    }

    /**
     * Remove o livro do carrinho.
     *
     * @throws ExcecaoDeRecursoNaoEncontrado se o livro não estiver no carrinho
     */
    public void remover(Long livroId) {
        ItemCarrinho item = itemExigido(livroId);
        itens.remove(item);
    }

    /** Estende o bloqueio do carrinho e das suas reservas (DEC-05.1, RN-VEN-014). */
    public void renovarExpiracao(LocalDateTime expiraEm) {
        this.expiraEm = Objects.requireNonNull(expiraEm, "expiraEm");
        this.avisoEnviadoEm = null;
    }

    /** Carrinho vazio deixa de ter bloqueio a expirar. */
    public void encerrarExpiracao() {
        this.expiraEm = null;
        this.avisoEnviadoEm = null;
    }

    public boolean vazio() {
        return itens.isEmpty();
    }

    private ItemCarrinho itemExigido(Long livroId) {
        return item(livroId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Item não encontrado no carrinho."));
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public List<ItemCarrinho> getItens() {
        return List.copyOf(itens);
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public LocalDateTime getAvisoEnviadoEm() {
        return avisoEnviadoEm;
    }
}
