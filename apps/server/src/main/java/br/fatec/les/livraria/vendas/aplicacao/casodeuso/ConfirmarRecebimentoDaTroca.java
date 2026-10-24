package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ReentrarEstoque;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.DetalharPedido;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCupomDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocaRepositorio;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.regra.RetornoAoEstoqueDeCadaItem;
import br.fatec.les.livraria.vendas.dominio.troca.ItemTroca;
import br.fatec.les.livraria.vendas.dominio.troca.RecebimentoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.RecebimentoDeTroca.ItemRecebido;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;
import br.fatec.les.livraria.vendas.dominio.troca.ValorDaTroca;
import lombok.RequiredArgsConstructor;

/**
 * O administrador confirma o recebimento dos itens de uma troca autorizada (RF-VEN-014, RF-VEN-015, RN-VEN-012,
 * DEC-11, DEC-12, DEC-13): informa se cada item volta ao estoque, os que voltam têm reentrada (RF-EST-004), a troca
 * passa a {@code TROCADO} e gera um cupom de troca do cliente no valor dos itens, também para os que não voltam. O
 * pedido continua {@code ENTREGUE}, e o cliente recebe {@code TROCA_CONCLUIDA}.
 *
 * <p>A troca é travada até o fim da transação; confirmar de novo responde 409, antes das regras do recebimento. A
 * reentrada vem antes do cupom, para que uma falha no estoque não chegue a gravar o código.</p>
 */
@Component
@RequiredArgsConstructor
public class ConfirmarRecebimentoDaTroca {

    private final TrocaRepositorio trocas;
    private final PedidosConsultas pedidos;
    private final CuponsDeTroca cuponsDeTroca;
    private final PropriedadesDoCupomDeTroca propriedadesDoCupom;
    private final ReentrarEstoque reentrarEstoque;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;
    private final DetalharPedido detalharPedido;
    private final Relogio relogio;
    private final RetornoAoEstoqueDeCadaItem retornoDeCadaItem; // RF-VEN-014

    /**
     * @param id    número da troca
     * @param itens se cada item da troca volta ao estoque
     * @return o pedido da troca, com as trocas atualizadas
     * @throws ExcecaoDeRecursoNaoEncontrado se a troca não existir
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito se a troca não estiver autorizada
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se faltar a decisão de algum item
     */
    @Transactional
    public PedidoDetalhado executar(Long id, List<ItemRecebido> itens) {
        Objects.requireNonNull(id, "id");
        Troca troca = trocas.buscarParaAlterar(id)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Troca não encontrada."));
        troca.exigirAutorizada();
        RecebimentoDeTroca recebimento = new RecebimentoDeTroca(troca, itens == null ? List.of() : itens);
        Violacoes.verificar(recebimento, retornoDeCadaItem);

        Pedido pedido = pedidos.buscar(troca.getPedidoId()).orElseThrow();
        Map<Long, Boolean> voltaEstoque = recebimento.voltaEstoquePorItem();
        Map<Long, Integer> devolvidos = devolvidosPorLivro(troca, pedido, voltaEstoque);
        if (!devolvidos.isEmpty()) {
            reentrarEstoque.executar(devolvidos);
        }
        LocalDateTime agora = relogio.agora();
        Dinheiro valor = ValorDaTroca.de(troca, pedido);
        CupomTroca cupom = cuponsDeTroca.gerar(new CupomTroca(null, pedido.getClienteId(), "TROCA-" + id, valor,
                propriedadesDoCupom.validadeDeUmCupomGeradoEm(agora.toLocalDate()), SituacaoCupom.DISPONIVEL,
                agora));
        troca.confirmarRecebimento(voltaEstoque, valor, cupom.id());
        trocas.salvar(troca);

        consultarUsuario.executar(pedido.getClienteId()).ifPresent(usuarioId -> notificar.executar(
                Notificar.Comando.sobre(usuarioId, TipoNotificacao.TROCA_CONCLUIDA,
                        "A troca nº %d do pedido nº %d foi concluída. O cupom de troca %s, de %s, já pode ser usado."
                                .formatted(id, pedido.getId(), cupom.codigo(), valor.emReais()),
                        "pedido", pedido.getId())));
        return detalharPedido.executar(pedido);
    }

    /** Unidades dos itens que voltam ao estoque, somadas por livro (RF-EST-004). */
    private static Map<Long, Integer> devolvidosPorLivro(Troca troca, Pedido pedido,
            Map<Long, Boolean> voltaEstoque) {
        Map<Long, Integer> porLivro = new HashMap<>();
        for (ItemTroca item : troca.getItens()) {
            if (Boolean.TRUE.equals(voltaEstoque.get(item.itemPedidoId()))) {
                porLivro.merge(pedido.item(item.itemPedidoId()).livro().id(), item.quantidade(), Integer::sum);
            }
        }
        return porLivro;
    }
}
