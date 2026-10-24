package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.NotificarAdministradores;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDaTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocaRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.regra.DentroDoPrazoDeTroca;
import br.fatec.les.livraria.vendas.dominio.regra.ItensDaTrocaInformados;
import br.fatec.les.livraria.vendas.dominio.regra.PedidoEntregueParaTroca;
import br.fatec.les.livraria.vendas.dominio.regra.QuantidadeDisponivelParaTroca;
import br.fatec.les.livraria.vendas.dominio.troca.ItemTroca;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca.ItemSolicitado;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;
import br.fatec.les.livraria.vendas.dominio.troca.TrocasDoPedido;
import lombok.RequiredArgsConstructor;

/**
 * O cliente pede a troca de itens de um pedido entregue, com a quantidade de cada um (RF-VEN-011, RN-VEN-011,
 * RN-VEN-013, DEC-12). A troca nasce {@code EM TROCA}, o pedido continua {@code ENTREGUE} e os administradores recebem
 * {@code TROCA_SOLICITADA} (DEC-13).
 *
 * <p>O pedido é travado antes de ler as trocas dele, para que duas solicitações ao mesmo tempo não usem a mesma
 * quantidade disponível. Pedido de outro cliente responde 404, antes de qualquer regra.</p>
 */
@Component
@RequiredArgsConstructor
public class SolicitarTroca {

    private final IdentificarCliente identificarCliente;
    private final PedidoRepositorio pedidos;
    private final TrocaRepositorio trocas;
    private final PropriedadesDaTroca propriedades;
    private final NotificarAdministradores notificarAdministradores;
    private final Relogio relogio;
    private final PedidoEntregueParaTroca pedidoEntregue; // RN-VEN-013
    private final DentroDoPrazoDeTroca dentroDoPrazo; // RN-VEN-013
    private final ItensDaTrocaInformados itensInformados; // RF-VEN-011
    private final QuantidadeDisponivelParaTroca quantidadeDisponivel; // RF-VEN-011

    /**
     * @param pedidoId número do pedido do cliente logado
     * @param itens    itens do pedido e quantidades a trocar
     * @return a troca {@code EM TROCA} gravada
     * @throws ExcecaoDeRecursoNaoEncontrado se o pedido não existir ou for de outro cliente
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se alguma regra da solicitação falhar
     */
    @Transactional
    public Troca executar(Long pedidoId, List<ItemSolicitado> itens) {
        Objects.requireNonNull(pedidoId, "pedidoId");
        Long clienteId = identificarCliente.executar();
        Pedido pedido = pedidos.buscarParaAlterar(pedidoId)
                .filter(encontrado -> encontrado.getClienteId().equals(clienteId))
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Pedido não encontrado."));
        SolicitacaoDeTroca solicitacao = new SolicitacaoDeTroca(
                new TrocasDoPedido(pedido, trocas.listarDoPedido(pedidoId)), itens == null ? List.of() : itens,
                propriedades.prazo(), relogio.agora());
        Violacoes.verificar(solicitacao, pedidoEntregue, dentroDoPrazo);
        Violacoes.verificar(solicitacao, itensInformados, quantidadeDisponivel);

        Troca troca = trocas.salvar(Troca.solicitar(pedidoId, solicitacao.itens().stream()
                .map(item -> new ItemTroca(null, item.itemPedidoId(), item.quantidade(), null))
                .toList(), solicitacao.agora()));
        notificarAdministradores.executar(NotificarAdministradores.Comando.sobre(TipoNotificacao.TROCA_SOLICITADA,
                "O pedido nº %d tem uma nova troca para autorizar.".formatted(pedidoId), "troca", troca.getId()));
        return troca;
    }
}
