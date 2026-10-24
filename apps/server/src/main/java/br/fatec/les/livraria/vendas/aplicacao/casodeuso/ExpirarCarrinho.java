package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.LiberarReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;
import lombok.RequiredArgsConstructor;

/**
 * Expira o carrinho cujo prazo de bloqueio venceu (RN-VEN-014, RN-VEN-015, RNF-VEN-001, DEC-13): libera as reservas
 * no {@code estoque}, retira todos os itens de cada livro, guarda-os nos itens removidos e notifica o cliente.
 *
 * <p>Trava o carrinho e confere de novo o vencimento, porque o cliente pode ter renovado o prazo entre a busca da
 * tarefa e esta transação.</p>
 */
@Component
@RequiredArgsConstructor
public class ExpirarCarrinho {

    static final String MENSAGEM = "O prazo de reserva acabou e os itens do seu carrinho foram removidos. Você pode "
            + "adicioná-los de novo.";

    private final CarrinhoRepositorio carrinhos;
    private final Relogio relogio;
    private final LiberarReserva liberarReserva;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;

    /** @param carrinhoId carrinho com o prazo vencido */
    @Transactional
    public void executar(Long carrinhoId) {
        LocalDateTime agora = relogio.agora();
        carrinhos.buscarParaAlterar(carrinhoId)
                .filter(carrinho -> carrinho.venceuEm(agora))
                .ifPresent(carrinho -> expirar(carrinho, agora));
    }

    private void expirar(Carrinho carrinho, LocalDateTime agora) {
        // RN-VEN-015: a linha é única por livro, então liberar a reserva retira todas as unidades do produto.
        for (ItemCarrinho item : carrinho.getItens()) {
            liberarReserva.executar(OrigemReserva.CARRINHO, carrinho.getId(), item.getLivroId());
        }
        carrinho.expirar(agora);
        carrinhos.salvar(carrinho);
        Long usuarioId = consultarUsuario.executar(carrinho.getClienteId());
        notificar.executar(Notificar.Comando.sobre(usuarioId, TipoNotificacao.CARRINHO_EXPIRADO, MENSAGEM,
                "carrinho", carrinho.getId()));
    }
}
