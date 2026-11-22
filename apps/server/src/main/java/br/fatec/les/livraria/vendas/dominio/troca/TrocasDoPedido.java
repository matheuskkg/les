package br.fatec.les.livraria.vendas.dominio.troca;

import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * As trocas de um pedido e os cálculos sobre elas (DEC-12). A troca "mais recente" é a de maior id, que cresce com a
 * solicitação; a data não desempata trocas pedidas no mesmo instante.
 */
public final class TrocasDoPedido {

    private static final Comparator<Troca> MAIS_RECENTE_PRIMEIRO =
            Comparator.comparing((Troca troca) -> Objects.requireNonNull(troca.getId(), "id")).reversed();

    private final Pedido pedido;
    private final List<Troca> trocas;

    /**
     * @param trocas trocas já gravadas do pedido, em qualquer ordem
     */
    public TrocasDoPedido(Pedido pedido, List<Troca> trocas) {
        this.pedido = Objects.requireNonNull(pedido, "pedido");
        this.trocas = Objects.requireNonNull(trocas, "trocas").stream().sorted(MAIS_RECENTE_PRIMEIRO).toList();
    }

    public Pedido pedido() {
        return pedido;
    }

    /** As trocas da mais recente para a mais antiga. */
    public List<Troca> trocas() {
        return trocas;
    }

    /**
     * Quantidade disponível para troca de um item (DEC-12): a comprada menos a que está em qualquer troca do pedido,
     * inclusive as recusadas, por isso um item recusado não pode ser pedido de novo.
     */
    public int quantidadeDisponivel(ItemPedido item) {
        int emTrocas = trocas.stream().mapToInt(troca -> troca.quantidadeDo(item.id())).sum();
        return Math.max(item.quantidade() - emTrocas, 0);
    }

    /** Status da troca mais recente que inclui o item, ou vazio se ele nunca foi trocado (DEC-12). */
    public Optional<StatusTroca> statusDoItem(ItemPedido item) {
        return trocas.stream().filter(troca -> troca.quantidadeDo(item.id()) > 0).findFirst().map(Troca::getStatus);
    }

    /**
     * Indicador {@code troca} do pedido (DEC-12): a situação é o status da troca mais recente, inclusive {@code TROCA
     * RECUSADA}; a abrangência é {@code TOTAL} se as trocas não recusadas cobrem toda a quantidade do pedido. Vazio
     * quando o pedido não tem troca.
     */
    public Optional<IndicadorDeTroca> indicador() {
        if (trocas.isEmpty()) {
            return Optional.empty();
        }
        boolean total =
                pedido.getItens().stream()
                        .allMatch(item ->
                                trocas.stream()
                                                .filter(troca -> !troca.recusada())
                                                .mapToInt(troca -> troca.quantidadeDo(item.id()))
                                                .sum()
                                        >= item.quantidade());
        return Optional.of(
                new IndicadorDeTroca(
                        trocas.getFirst().getStatus(), total ? AbrangenciaDaTroca.TOTAL : AbrangenciaDaTroca.PARCIAL));
    }
}
