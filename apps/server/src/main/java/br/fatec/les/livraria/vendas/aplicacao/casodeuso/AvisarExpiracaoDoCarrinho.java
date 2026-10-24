package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import lombok.RequiredArgsConstructor;

/**
 * Avisa o cliente de que o bloqueio do carrinho está perto de vencer (RN-VEN-014, DEC-13). O aviso sai uma vez por
 * prazo: renovar o prazo permite um novo aviso.
 *
 * <p>Trava o carrinho e confere de novo se o aviso cabe, porque o cliente pode ter renovado o prazo entre a busca da
 * tarefa e esta transação.</p>
 */
@Component
@RequiredArgsConstructor
public class AvisarExpiracaoDoCarrinho {

    static final String MENSAGEM = "A reserva dos itens do seu carrinho está perto de expirar. Finalize a compra ou "
            + "inclua um item para renovar o prazo.";

    private final CarrinhoRepositorio carrinhos;
    private final PropriedadesDoCarrinho propriedades;
    private final Relogio relogio;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;

    /** @param carrinhoId carrinho cujo prazo está perto de vencer */
    @Transactional
    public void executar(Long carrinhoId) {
        LocalDateTime agora = relogio.agora();
        carrinhos.buscarParaAlterar(carrinhoId)
                .filter(carrinho -> carrinho.deveAvisarEm(agora, propriedades.avisoExpiracao()))
                .ifPresent(carrinho -> avisar(carrinho, agora));
    }

    private void avisar(Carrinho carrinho, LocalDateTime agora) {
        carrinho.registrarAviso(agora);
        carrinhos.salvar(carrinho);
        consultarUsuario.executar(carrinho.getClienteId()).ifPresent(usuarioId -> notificar.executar(Notificar.Comando.sobre(usuarioId, TipoNotificacao.CARRINHO_EXPIRANDO, MENSAGEM,
                "carrinho", carrinho.getId())));
    }
}
