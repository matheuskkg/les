package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.DetalharPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocaRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.regra.MotivoDaRecusaInformado;
import br.fatec.les.livraria.vendas.dominio.regra.MotivoDaRecusaNoLimite;
import br.fatec.les.livraria.vendas.dominio.troca.RecusaDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;
import lombok.RequiredArgsConstructor;

/**
 * O administrador recusa uma troca com motivo: {@code EM TROCA} → {@code TROCA RECUSADA} (DEC-12). As unidades
 * recusadas não voltam a ficar disponíveis para troca, e o cliente recebe {@code TROCA_RECUSADA} com o motivo
 * (DEC-13). A troca é travada até o fim da transação; autorizar ou recusar de novo responde 409.
 */
@Component
@RequiredArgsConstructor
public class RecusarTroca {

    private final TrocaRepositorio trocas;
    private final PedidosConsultas pedidos;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;
    private final DetalharPedido detalharPedido;
    private final MotivoDaRecusaInformado motivoInformado; // RF-VEN-012
    private final MotivoDaRecusaNoLimite motivoNoLimite; // RF-VEN-012

    /**
     * @param id     número da troca
     * @param motivo motivo da recusa, enviado ao cliente
     * @return o pedido da troca, com as trocas atualizadas
     * @throws ExcecaoDeRecursoNaoEncontrado se a troca não existir
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito se a troca não estiver {@code EM TROCA}
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se o motivo faltar ou passar do limite
     */
    @Transactional
    public PedidoDetalhado executar(Long id, @Nullable String motivo) {
        Objects.requireNonNull(id, "id");
        Troca troca = trocas.buscarParaAlterar(id)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Troca não encontrada."));
        Violacoes.verificar(new RecusaDeTroca(troca, motivo), motivoInformado, motivoNoLimite);
        troca.recusar(Objects.requireNonNull(motivo));
        Troca recusada = trocas.salvar(troca);
        Pedido pedido = pedidos.buscar(troca.getPedidoId()).orElseThrow();
        consultarUsuario.executar(pedido.getClienteId()).ifPresent(usuarioId -> notificar.executar(
                Notificar.Comando.sobre(usuarioId, TipoNotificacao.TROCA_RECUSADA,
                        "A troca nº %d do pedido nº %d foi recusada. Motivo: %s"
                                .formatted(id, pedido.getId(), recusada.getMotivoRecusa()),
                        "pedido", pedido.getId())));
        return detalharPedido.executar(pedido);
    }
}
