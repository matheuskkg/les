package br.fatec.les.livraria.vendas.dominio.modelo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;

/**
 * Sessão de compra entre o carrinho e o pedido (RF-VEN-003, DEC-12). Segura as reservas dos itens até o prazo
 * {@code expiraEm}, contado do início; não guarda endereços, cartões nem cupons, que cada finalização recebe na
 * requisição (DEC-16).
 */
public class Checkout {

    private final Long id;
    private final Long clienteId;
    private final List<ItemCheckout> itens;
    private final LocalDateTime iniciadoEm;
    private final LocalDateTime expiraEm;
    private final Long versao;
    private SituacaoCheckout situacao;
    private LocalDateTime avisoEnviadoEm;

    public Checkout(Long id, Long clienteId, SituacaoCheckout situacao, LocalDateTime iniciadoEm,
            LocalDateTime expiraEm, LocalDateTime avisoEnviadoEm, Long versao, List<ItemCheckout> itens) {
        this.id = id;
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId");
        this.situacao = Objects.requireNonNull(situacao, "situacao");
        this.iniciadoEm = Objects.requireNonNull(iniciadoEm, "iniciadoEm");
        this.expiraEm = Objects.requireNonNull(expiraEm, "expiraEm");
        this.avisoEnviadoEm = avisoEnviadoEm;
        this.versao = versao;
        this.itens = List.copyOf(Objects.requireNonNull(itens, "itens"));
    }

    /**
     * Abre o checkout com os itens do carrinho (DEC-12).
     *
     * @throws IllegalArgumentException se não houver itens
     */
    public static Checkout iniciar(Long clienteId, List<ItemCheckout> itens, LocalDateTime agora, Duration prazo) {
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("O checkout precisa de ao menos um item.");
        }
        return new Checkout(null, clienteId, SituacaoCheckout.ABERTO, agora, agora.plus(prazo), null, null, itens);
    }

    public boolean aberto() {
        return situacao == SituacaoCheckout.ABERTO;
    }

    /** RN-VEN-014: o prazo do checkout aberto venceu. */
    public boolean venceuEm(LocalDateTime agora) {
        return aberto() && !expiraEm.isAfter(agora);
    }

    /** RN-VEN-014: o aviso sai uma vez, quando faltar a antecedência ou menos; vencido, não há aviso atrasado. */
    public boolean deveAvisarEm(LocalDateTime agora, Duration antecedencia) {
        return aberto() && avisoEnviadoEm == null && expiraEm.isAfter(agora)
                && !expiraEm.isAfter(agora.plus(antecedencia));
    }

    public void registrarAviso(LocalDateTime agora) {
        this.avisoEnviadoEm = Objects.requireNonNull(agora, "agora");
    }

    /**
     * Encerra o checkout aberto: o cliente voltou ao carrinho ou inativou a conta (DEC-10, DEC-12).
     *
     * @throws ExcecaoDeConflito se o checkout não estiver aberto
     */
    public void encerrar() {
        exigirAberto();
        situacao = SituacaoCheckout.ENCERRADO;
    }

    /**
     * Expira o checkout com o prazo vencido (RN-VEN-014, DEC-12). As reservas são liberadas pelo {@code estoque} e os
     * itens vão para os removidos do carrinho.
     *
     * @throws ExcecaoDeConflito se o checkout não estiver aberto
     */
    public void expirar() {
        exigirAberto();
        situacao = SituacaoCheckout.EXPIRADO;
    }

    /**
     * Conclui o checkout do pedido aprovado: as reservas viraram baixa de estoque (DEC-12).
     *
     * @throws ExcecaoDeConflito se o checkout não estiver aberto
     */
    public void concluir() {
        exigirAberto();
        situacao = SituacaoCheckout.CONCLUIDO;
    }

    /** Itens retirados por expiração, para os removidos do carrinho (RNF-VEN-001). */
    public List<ItemRemovidoCarrinho> itensRemovidosEm(LocalDateTime agora) {
        return itens.stream()
                .map(item -> new ItemRemovidoCarrinho(item.livroId(), item.quantidade(), agora))
                .toList();
    }

    private void exigirAberto() {
        if (!aberto()) {
            throw new ExcecaoDeConflito("A sessão de compra não está aberta.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public SituacaoCheckout getSituacao() {
        return situacao;
    }

    public LocalDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public LocalDateTime getAvisoEnviadoEm() {
        return avisoEnviadoEm;
    }

    public Long getVersao() {
        return versao;
    }

    public List<ItemCheckout> getItens() {
        return itens;
    }
}
