package br.fatec.les.livraria.vendas.dominio.troca;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * O "pedido de troca" do RN-VEN-011 (DEC-12): itens de um pedido entregue com a quantidade de cada um. Um pedido pode
 * ter várias trocas; o status do pedido não muda.
 */
public class Troca {

    private final @Nullable Long id;
    private final Long pedidoId;
    private final LocalDateTime solicitadaEm;
    private List<ItemTroca> itens;
    private StatusTroca status;
    private @Nullable String motivoRecusa;
    private @Nullable Dinheiro valorCupom;
    private @Nullable Long cupomId;

    public Troca(
            @Nullable Long id,
            Long pedidoId,
            StatusTroca status,
            LocalDateTime solicitadaEm,
            List<ItemTroca> itens,
            @Nullable String motivoRecusa,
            @Nullable Dinheiro valorCupom,
            @Nullable Long cupomId) {
        this.id = id;
        this.pedidoId = Objects.requireNonNull(pedidoId, "pedidoId");
        this.status = Objects.requireNonNull(status, "status");
        this.solicitadaEm = Objects.requireNonNull(solicitadaEm, "solicitadaEm");
        this.itens = List.copyOf(Objects.requireNonNull(itens, "itens"));
        this.motivoRecusa = motivoRecusa;
        this.valorCupom = valorCupom;
        this.cupomId = cupomId;
    }

    /**
     * Cria a troca {@code EM TROCA} pedida pelo cliente (RF-VEN-011, RN-VEN-011). As regras da solicitação ficam em
     * {@code vendas.dominio.regra}.
     *
     * @throws IllegalArgumentException se não houver itens
     */
    public static Troca solicitar(Long pedidoId, List<ItemTroca> itens, LocalDateTime agora) {
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("A troca precisa de ao menos um item.");
        }
        return new Troca(null, pedidoId, StatusTroca.EM_TROCA, agora, itens, null, null, null);
    }

    /** Quantidade deste item do pedido incluída na troca; zero se ele não faz parte dela. */
    public int quantidadeDo(Long itemPedidoId) {
        return itens.stream()
                .filter(item -> item.itemPedidoId().equals(itemPedidoId))
                .mapToInt(ItemTroca::quantidade)
                .sum();
    }

    /**
     * O administrador autoriza a troca: {@code EM TROCA} → {@code TROCA AUTORIZADA} (RF-VEN-012, DEC-12).
     *
     * @throws ExcecaoDeConflito se a troca não estiver {@code EM TROCA}
     */
    public void autorizar() {
        exigirEmTroca("autorizada");
        status = StatusTroca.TROCA_AUTORIZADA;
    }

    /**
     * O administrador recusa a troca com um motivo: {@code EM TROCA} → {@code TROCA RECUSADA} (DEC-12). As regras do
     * motivo ficam em {@code vendas.dominio.regra}.
     *
     * @throws ExcecaoDeConflito se a troca não estiver {@code EM TROCA}
     */
    public void recusar(String motivo) {
        Objects.requireNonNull(motivo, "motivo");
        exigirEmTroca("recusada");
        status = StatusTroca.TROCA_RECUSADA;
        motivoRecusa = motivo.strip();
    }

    /**
     * O administrador confirma o recebimento dos itens: {@code TROCA AUTORIZADA} → {@code TROCADO} (RF-VEN-014,
     * RF-VEN-015, RN-VEN-012, DEC-12). Guarda se cada item volta ao estoque, o valor e o cupom gerado. As regras do
     * recebimento ficam em {@code vendas.dominio.regra}.
     *
     * @param voltaEstoque decisão por item do pedido; precisa cobrir todos os itens da troca
     * @throws ExcecaoDeConflito se a troca não estiver {@code TROCA AUTORIZADA}
     * @throws IllegalArgumentException se faltar a decisão de algum item
     */
    public void confirmarRecebimento(Map<Long, Boolean> voltaEstoque, Dinheiro valor, Long cupomGerado) {
        Objects.requireNonNull(voltaEstoque, "voltaEstoque");
        exigirAutorizada();
        for (ItemTroca item : itens) {
            if (voltaEstoque.get(item.itemPedidoId()) == null) {
                throw new IllegalArgumentException(
                        "Falta informar se o item %d volta ao estoque.".formatted(item.itemPedidoId()));
            }
        }
        itens =
                itens.stream()
                        .map(item ->
                                new ItemTroca(
                                        item.id(),
                                        item.itemPedidoId(),
                                        item.quantidade(),
                                        voltaEstoque.get(item.itemPedidoId())))
                        .toList();
        status = StatusTroca.TROCADO;
        valorCupom = Objects.requireNonNull(valor, "valor");
        cupomId = Objects.requireNonNull(cupomGerado, "cupomGerado");
    }

    /**
     * Confere, antes das regras do recebimento, que a troca está {@code TROCA AUTORIZADA} (DEC-12).
     *
     * @throws ExcecaoDeConflito se não estiver
     */
    public void exigirAutorizada() {
        if (status != StatusTroca.TROCA_AUTORIZADA) {
            throw new ExcecaoDeConflito("Só uma troca autorizada pode ter o recebimento confirmado.");
        }
    }

    /**
     * Confere, antes das regras da ação, que a troca ainda está {@code EM TROCA} (DEC-12).
     *
     * @param acao particípio da ação, para a mensagem ("autorizada", "recusada")
     * @throws ExcecaoDeConflito se a troca não estiver {@code EM TROCA}
     */
    public void exigirEmTroca(String acao) {
        if (status != StatusTroca.EM_TROCA) {
            throw new ExcecaoDeConflito("Só uma troca EM TROCA pode ser %s.".formatted(acao));
        }
    }

    public boolean recusada() {
        return status == StatusTroca.TROCA_RECUSADA;
    }

    public @Nullable Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public StatusTroca getStatus() {
        return status;
    }

    public LocalDateTime getSolicitadaEm() {
        return solicitadaEm;
    }

    public List<ItemTroca> getItens() {
        return itens;
    }

    public @Nullable String getMotivoRecusa() {
        return motivoRecusa;
    }

    public @Nullable Dinheiro getValorCupom() {
        return valorCupom;
    }

    public @Nullable Long getCupomId() {
        return cupomId;
    }
}
