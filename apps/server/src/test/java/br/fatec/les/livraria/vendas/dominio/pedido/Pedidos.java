package br.fatec.les.livraria.vendas.dominio.pedido;

import java.time.LocalDateTime;
import java.util.List;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/** Pedidos de exemplo para os testes, com todas as cópias preenchidas. */
public final class Pedidos {

    public static final LocalDateTime REALIZADO_EM = LocalDateTime.of(2026, 10, 24, 12, 0);

    private Pedidos() {
    }

    /** Dois Dom Casmurro (R$ 47,88) com frete de R$ 12,00, BEMVINDO10, o cupom de troca 2 e um Visa. */
    public static Pedido emProcessamento(Long checkoutId, Long clienteId) {
        LivroNoPedido livro = new LivroNoPedido(1L, "Dom Casmurro", List.of("Machado de Assis"), "Editora Garnier",
                "1ª", "9788535910663", List.of(new CategoriaNoPedido(1L, "Romance"),
                        new CategoriaNoPedido(2L, "Clássicos")));
        EnderecoNoPedido endereco = new EnderecoNoPedido(1L, "Casa", "RESIDENCIAL", "RUA", "Rua Oscar Freire", "100",
                "Jardins", "01415000", "São Paulo", "SP", "Brasil", null);
        return Pedido.registrar(checkoutId, clienteId, REALIZADO_EM,
                new ClienteNoPedido(clienteId, "Ana Souza", "52998224725", "ana@exemplo.com", "(11) 98888-7001"),
                endereco, endereco,
                List.of(new ItemPedido(null, livro, Dinheiro.de("47.88"), 2)),
                List.of(new PagamentoCartao(new CartaoNoPedido(1L, "Visa", "1111"), Dinheiro.de("57.76"))),
                List.of(new CupomPedido(new CupomNoPedido(1L, "BEMVINDO10", CategoriaCupom.PROMOCIONAL, "VALOR_FIXO"),
                        Dinheiro.de("10.00")),
                        new CupomPedido(new CupomNoPedido(2L, "TROCA-ANA-40", CategoriaCupom.TROCA, null),
                                Dinheiro.de("40.00"))),
                Dinheiro.de("12.00"), Dinheiro.de("10.00"));
    }
}
