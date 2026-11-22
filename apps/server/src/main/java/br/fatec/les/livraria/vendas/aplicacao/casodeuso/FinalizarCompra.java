package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CartaoRecusado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CompraFinalizada;
import br.fatec.les.livraria.vendas.aplicacao.porta.OperadoraDeCartao;
import br.fatec.les.livraria.vendas.dominio.pagamento.CobrancaNoCartao;
import br.fatec.les.livraria.vendas.dominio.pagamento.RespostaDaOperadora;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * "Finalizar compra" (RF-VEN-008, RN-VEN-007, RN-VEN-008, DEC-05.2, DEC-13). Não é transacional: grava o pedido {@code
 * EM PROCESSAMENTO} numa transação, chama a operadora fora de transação, com o número e o código de segurança só em
 * memória, e aprova ou reprova o pedido numa segunda transação. A auditoria registra as duas transições.
 *
 * <p>Se a segunda transação falhar, o pedido fica em processamento até o checkout acabar, quando é reprovado.
 */
@Component
@RequiredArgsConstructor
public class FinalizarCompra {

    private final RegistrarPedido registrarPedido;
    private final OperadoraDeCartao operadora;
    private final AprovarPedido aprovarPedido;
    private final ReprovarPedido reprovarPedido;
    private final Relogio relogio;

    public CompraFinalizada executar(FinalizacaoInformada finalizacao) {
        PedidoRegistrado registrado = registrarPedido.executar(finalizacao);
        LocalDate hoje = relogio.hoje();
        List<CartaoRecusado> recusas = new ArrayList<>();
        for (CobrancaNoCartao cobranca : registrado.cobrancas()) {
            RespostaDaOperadora resposta = operadora.cobrar(cobranca, hoje);
            if (!resposta.aprovada()) {
                recusas.add(new CartaoRecusado(cobranca.ultimosDigitos(), resposta.motivo()));
            }
        }
        Pedido pedido =
                recusas.isEmpty()
                        ? aprovarPedido.executar(registrado.pedido(), registrado.troco())
                        : reprovarPedido.executar(registrado.pedido(), motivo(recusas));
        return new CompraFinalizada(pedido, recusas);
    }

    private static String motivo(List<CartaoRecusado> recusas) {
        return recusas.stream()
                .map(recusa ->
                        "cartão final %s, %s"
                                .formatted(recusa.ultimosDigitos(), recusa.motivo().toLowerCase(Locale.ROOT)))
                .collect(Collectors.joining("; "));
    }
}
