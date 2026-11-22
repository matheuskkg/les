package br.fatec.les.livraria.vendas.dominio.pedido;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.time.LocalDateTime;
import java.util.List;

/** Pedidos de exemplo para os testes, com todas as cópias preenchidas. */
public final class Pedidos {

    public static final LocalDateTime REALIZADO_EM = LocalDateTime.of(2026, 10, 24, 12, 0);

    private Pedidos() {}

    /** Dois Dom Casmurro (R$ 47,88) com frete de R$ 12,00, BEMVINDO10, o cupom de troca 2 e um Visa. */
    public static Pedido emProcessamento(Long checkoutId, Long clienteId) {
        return emProcessamento(
                checkoutId,
                new ClienteNoPedido(clienteId, "Ana Souza", "52998224725", "ana@exemplo.com", "(11) 98888-7001"),
                REALIZADO_EM);
    }

    /** O mesmo pedido de {@link #emProcessamento(Long, Long)}, com outro cliente e outra data de finalização. */
    public static Pedido emProcessamento(Long checkoutId, ClienteNoPedido cliente, LocalDateTime realizadoEm) {
        LivroNoPedido livro =
                new LivroNoPedido(
                        1L,
                        "Dom Casmurro",
                        List.of("Machado de Assis"),
                        "Editora Garnier",
                        "1ª",
                        "9788535910663",
                        List.of(new CategoriaNoPedido(1L, "Romance"), new CategoriaNoPedido(2L, "Clássicos")));
        EnderecoNoPedido endereco =
                new EnderecoNoPedido(
                        1L,
                        "Casa",
                        "RESIDENCIAL",
                        "RUA",
                        "Rua Oscar Freire",
                        "100",
                        "Jardins",
                        "01415000",
                        "São Paulo",
                        "SP",
                        "Brasil",
                        null);
        return Pedido.registrar(
                checkoutId,
                cliente.id(),
                realizadoEm,
                cliente,
                endereco,
                endereco,
                List.of(new ItemPedido(null, livro, Dinheiro.de("47.88"), 2)),
                List.of(new PagamentoCartao(new CartaoNoPedido(1L, "Visa", "1111"), Dinheiro.de("57.76"))),
                List.of(
                        new CupomPedido(
                                new CupomNoPedido(1L, "BEMVINDO10", CategoriaCupom.PROMOCIONAL, "VALOR_FIXO"),
                                Dinheiro.de("10.00")),
                        new CupomPedido(
                                new CupomNoPedido(2L, "TROCA-ANA-40", CategoriaCupom.TROCA, null),
                                Dinheiro.de("40.00"))),
                Dinheiro.de("12.00"),
                Dinheiro.de("10.00"));
    }

    /**
     * Pedido gravado e entregue em {@code entregueEm}, com o item 101 (dois Dom Casmurro) e o item 102 (um Memórias
     * Póstumas de Brás Cubas).
     */
    public static Pedido entregue(LocalDateTime entregueEm) {
        Pedido base = emProcessamento(10L, 1L);
        LivroNoPedido memorias =
                new LivroNoPedido(
                        2L,
                        "Memórias Póstumas de Brás Cubas",
                        List.of("Machado de Assis"),
                        "Editora Garnier",
                        "1ª",
                        "9788535911664",
                        List.of(new CategoriaNoPedido(1L, "Romance")));
        List<ItemPedido> itens =
                List.of(
                        new ItemPedido(101L, base.getItens().getFirst().livro(), Dinheiro.de("47.88"), 2),
                        new ItemPedido(102L, memorias, Dinheiro.de("39.90"), 1));
        return new Pedido(
                1L,
                10L,
                1L,
                StatusPedido.ENTREGUE,
                REALIZADO_EM,
                entregueEm,
                base.getCliente(),
                base.getEnderecoEntrega(),
                base.getEnderecoCobranca(),
                itens,
                base.getCartoes(),
                base.getCupons(),
                Dinheiro.de("135.66"),
                base.getFrete(),
                base.getDescontoPromocional(),
                Dinheiro.de("137.66"));
    }
}
