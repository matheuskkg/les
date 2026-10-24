package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.aplicacao.TarefaPorTempo;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tarefa periódica de expiração (RN-VEN-014, DEC-13): expira os carrinhos vencidos e avisa os que estão perto de
 * vencer. Não é transacional: cada carrinho é tratado na própria transação, e a falha de um não impede os outros.
 *
 * <p>O agendamento roda a cada 30 s; o profile {@code e2e} também a executa ao mudar o relógio (DEC-09).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirarReservas implements TarefaPorTempo {

    private final CarrinhoRepositorio carrinhos;
    private final PropriedadesDoCarrinho propriedades;
    private final Relogio relogio;
    private final ExpirarCarrinho expirarCarrinho;
    private final AvisarExpiracaoDoCarrinho avisarExpiracao;

    @Override
    public void executar() {
        LocalDateTime agora = relogio.agora();
        // Expira antes de avisar: um carrinho vencido não recebe aviso atrasado.
        carrinhos.listarIdsVencidos(agora).forEach(id -> tratar(id, expirarCarrinho::executar, "expirar"));
        carrinhos.listarIdsParaAvisar(agora, agora.plus(propriedades.avisoExpiracao()))
                .forEach(id -> tratar(id, avisarExpiracao::executar, "avisar a expiração do"));
    }

    private static void tratar(Long carrinhoId, Consumer<Long> acao, String descricao) {
        try {
            acao.accept(carrinhoId);
        } catch (RuntimeException excecao) {
            log.warn("Falha ao {} carrinho {}", descricao, carrinhoId, excecao);
        }
    }
}
