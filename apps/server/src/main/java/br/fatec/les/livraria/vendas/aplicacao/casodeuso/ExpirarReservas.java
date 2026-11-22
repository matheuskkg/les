package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.compartilhado.aplicacao.TarefaPorTempo;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCheckout;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Tarefa periódica de expiração (RN-VEN-014, DEC-12, DEC-13): expira os carrinhos e os checkouts vencidos e avisa os
 * que estão perto de vencer. Não é transacional e deve ser chamada fora de uma transação, como fazem o agendamento e o
 * {@code POST /api/teste/relogio}: assim cada carrinho é tratado na própria transação, e a falha de um não desfaz os
 * outros.
 *
 * <p>O agendamento roda a cada 30 s; o profile {@code e2e} também a executa ao mudar o relógio (DEC-09).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirarReservas implements TarefaPorTempo {

    private final CarrinhoRepositorio carrinhos;
    private final CheckoutRepositorio checkouts;
    private final PropriedadesDoCarrinho propriedadesDoCarrinho;
    private final PropriedadesDoCheckout propriedadesDoCheckout;
    private final Relogio relogio;
    private final ExpirarCarrinho expirarCarrinho;
    private final AvisarExpiracaoDoCarrinho avisarExpiracaoDoCarrinho;
    private final ExpirarCheckout expirarCheckout;
    private final AvisarExpiracaoDoCheckout avisarExpiracaoDoCheckout;

    @Override
    public void executar() {
        LocalDateTime agora = relogio.agora();
        // Expira antes de avisar: um carrinho vencido não recebe aviso atrasado.
        carrinhos.listarIdsVencidos(agora).forEach(id -> tratar(id, expirarCarrinho::executar, "expirar o carrinho"));
        carrinhos
                .listarIdsParaAvisar(agora, agora.plus(propriedadesDoCarrinho.avisoExpiracao()))
                .forEach(id -> tratar(id, avisarExpiracaoDoCarrinho::executar, "avisar a expiração do carrinho"));
        checkouts.listarIdsVencidos(agora).forEach(id -> tratar(id, expirarCheckout::executar, "expirar o checkout"));
        checkouts
                .listarIdsParaAvisar(agora, agora.plus(propriedadesDoCheckout.avisoExpiracao()))
                .forEach(id -> tratar(id, avisarExpiracaoDoCheckout::executar, "avisar a expiração do checkout"));
    }

    private static void tratar(Long id, Consumer<Long> acao, String descricao) {
        try {
            acao.accept(id);
        } catch (RuntimeException excecao) {
            log.warn("Falha ao {} {}", descricao, id, excecao);
        }
    }
}
