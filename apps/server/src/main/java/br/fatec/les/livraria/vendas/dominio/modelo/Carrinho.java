package br.fatec.les.livraria.vendas.dominio.modelo;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Carrinho de compras do cliente (RF-VEN-001, RF-VEN-002, DEC-12, DEC-16). Há um carrinho por cliente, criado na
 * primeira adição. O prazo de bloqueio ({@code expiraEm}) é contado a partir do último item incluído (DEC-05.1) e as
 * reservas de estoque acompanham esse prazo.
 *
 * <p>Vencido o prazo, os itens saem do carrinho e ficam nos itens removidos, à mostra até o cliente adicionar o livro
 * de novo ou dispensar o aviso (RN-VEN-014, RN-VEN-015, RNF-VEN-001).
 *
 * <p>O carrinho não guarda preço: a leitura usa o preço de venda atual do catálogo. O pedido é que copia os dados na
 * finalização (DEC-12, DEC-16).
 */
public class Carrinho {

    private final Long id;
    private final Long clienteId;
    private final List<ItemCarrinho> itens;
    private final List<ItemRemovidoCarrinho> removidos;
    private LocalDateTime expiraEm;
    private LocalDateTime avisoEnviadoEm;

    public Carrinho(
            Long id,
            Long clienteId,
            LocalDateTime expiraEm,
            LocalDateTime avisoEnviadoEm,
            List<ItemCarrinho> itens,
            List<ItemRemovidoCarrinho> removidos) {
        this.id = id;
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId");
        this.itens = new ArrayList<>(Objects.requireNonNull(itens, "itens"));
        this.removidos = new ArrayList<>(Objects.requireNonNull(removidos, "removidos"));
        this.expiraEm = expiraEm;
        this.avisoEnviadoEm = avisoEnviadoEm;
    }

    /** Carrinho ainda sem id e sem itens, para o primeiro item do cliente (criação preguiçosa). */
    public static Carrinho vazio(Long clienteId) {
        return new Carrinho(null, clienteId, null, null, List.of(), List.of());
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
     * Adiciona um livro ou soma à linha existente (RF-VEN-001). A linha é única por livro (DEC-16). Um livro que estava
     * nos itens removidos sai de lá, porque foi adicionado de novo (RNF-VEN-001).
     *
     * @throws IllegalArgumentException se a quantidade não for positiva
     */
    public void adicionar(Long livroId, int quantidade) {
        Objects.requireNonNull(livroId, "livroId");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade adicionada deve ser no mínimo 1.");
        }
        item(livroId)
                .ifPresentOrElse(
                        item -> item.somar(quantidade), () -> itens.add(new ItemCarrinho(null, livroId, quantidade)));
        removidos.removeIf(removido -> removido.livroId().equals(livroId));
    }

    /**
     * Define a quantidade de um item existente (RF-VEN-002). A remoção é feita por {@link #remover(Long)}, porque zero
     * não é uma quantidade válida.
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

    /** RN-VEN-014: o prazo venceu e ainda há itens bloqueados a retirar. */
    public boolean venceuEm(LocalDateTime agora) {
        return !itens.isEmpty() && expiraEm != null && !expiraEm.isAfter(agora);
    }

    /**
     * RN-VEN-014: o aviso sai uma vez, quando faltar a antecedência ou menos para o vencimento. Um carrinho já vencido
     * não recebe aviso atrasado: ele é expirado.
     */
    public boolean deveAvisarEm(LocalDateTime agora, Duration antecedencia) {
        return !itens.isEmpty()
                && expiraEm != null
                && avisoEnviadoEm == null
                && expiraEm.isAfter(agora)
                && !expiraEm.isAfter(agora.plus(antecedencia));
    }

    /** Registra que o aviso de expiração foi enviado, para não repeti-lo no mesmo prazo (DEC-16). */
    public void registrarAviso(LocalDateTime agora) {
        this.avisoEnviadoEm = Objects.requireNonNull(agora, "agora");
    }

    /**
     * Retira todos os itens por vencimento do prazo e os guarda nos itens removidos (RN-VEN-014, RN-VEN-015,
     * RNF-VEN-001). As reservas devem ser liberadas antes, pelo {@code estoque}.
     */
    public void expirar(LocalDateTime agora) {
        Objects.requireNonNull(agora, "agora");
        List<ItemRemovidoCarrinho> retirados =
                itens.stream()
                        .map(item -> new ItemRemovidoCarrinho(item.getLivroId(), item.getQuantidade(), agora))
                        .toList();
        itens.clear();
        registrarRemovidos(retirados);
        encerrarExpiracao();
    }

    /**
     * Guarda itens retirados por expiração, um por livro (DEC-16): o registro mais recente de um livro substitui o
     * anterior. Um livro que já está de novo entre os itens do carrinho não vira aviso, como na expiração de um
     * checkout depois de o cliente ter readicionado o livro.
     */
    public void registrarRemovidos(List<ItemRemovidoCarrinho> novos) {
        for (ItemRemovidoCarrinho novo : novos) {
            if (item(novo.livroId()).isPresent()) {
                continue;
            }
            removidos.removeIf(removido -> removido.livroId().equals(novo.livroId()));
            removidos.add(novo);
        }
    }

    /**
     * Dispensa o aviso de um item removido (RNF-VEN-001).
     *
     * @throws ExcecaoDeRecursoNaoEncontrado se o livro não estiver nos itens removidos
     */
    public void dispensarRemovido(Long livroId) {
        Objects.requireNonNull(livroId, "livroId");
        if (!removidos.removeIf(removido -> removido.livroId().equals(livroId))) {
            throw new ExcecaoDeRecursoNaoEncontrado("Item não encontrado entre os removidos do carrinho.");
        }
    }

    /**
     * Retira os itens para o checkout (DEC-12): o carrinho fica vazio, sem prazo, e pode receber itens novos. Os itens
     * removidos por expiração continuam à mostra.
     *
     * @return os itens retirados
     */
    public List<ItemCarrinho> retirarItens() {
        List<ItemCarrinho> retirados = List.copyOf(itens);
        itens.clear();
        encerrarExpiracao();
        return retirados;
    }

    /** Esvazia o carrinho e os itens removidos, como na inativação da conta (DEC-10). */
    public void esvaziar() {
        itens.clear();
        removidos.clear();
        encerrarExpiracao();
    }

    private ItemCarrinho itemExigido(Long livroId) {
        return item(livroId).orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Item não encontrado no carrinho."));
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

    public List<ItemRemovidoCarrinho> getRemovidos() {
        return List.copyOf(removidos);
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public LocalDateTime getAvisoEnviadoEm() {
        return avisoEnviadoEm;
    }
}
