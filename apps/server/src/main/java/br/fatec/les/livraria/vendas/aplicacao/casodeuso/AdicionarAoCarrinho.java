package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.aplicacao.MontarCarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.ReservarItensDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.regra.LivroDoCarrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;
import br.fatec.les.livraria.vendas.dominio.regra.QuantidadeDoItem;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Adiciona um livro ao carrinho do cliente, somando à linha existente (RF-VEN-001, RF-VEN-002, RN-VEN-001).
 *
 * <p>Valida a quantidade e o livro, trava o carrinho, renova o prazo do bloqueio, contado do último item incluído
 * (DEC-05.1, RN-VEN-014), e reserva de novo todos os itens no {@code estoque} na mesma transação (DEC-13). Se faltar
 * estoque para algum item, a transação é desfeita e nada é alterado.
 */
@Component
@RequiredArgsConstructor
public class AdicionarAoCarrinho {

    private final IdentificarCliente identificarCliente;
    private final QuantidadeDoItem quantidadeDoItem;
    private final LivroDoCarrinho livroDoCarrinho;
    private final CarrinhoRepositorio carrinhos;
    private final PropriedadesDoCarrinho propriedades;
    private final Relogio relogio;
    private final ReservarItensDoCarrinho reservarItens;
    private final MontarCarrinhoConsultado montarCarrinho;

    /**
     * @param comando livro e quantidade a adicionar
     * @return o carrinho atualizado
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações de quantidade, livro não
     *     vendável ou estoque insuficiente
     */
    @Transactional
    public CarrinhoConsultado executar(AdicaoDeItem comando) {
        Long clienteId = identificarCliente.executar();
        ItemProposto proposta = new ItemProposto(comando.livroId(), comando.quantidade());
        Violacoes.verificar(proposta, quantidadeDoItem, livroDoCarrinho);

        Carrinho carrinho =
                carrinhos
                        .buscarPorClienteParaAlterar(clienteId)
                        .orElseGet(() -> carrinhos.salvar(Carrinho.vazio(clienteId)));
        try {
            carrinho.adicionar(proposta.livroId(), proposta.quantidade());
        } catch (ArithmeticException excecao) {
            throw new ExcecaoDeNegocio("RN-VEN-001", "Quantidade acima do estoque disponível.");
        }

        // DEC-05.1: o prazo conta do último item incluído e vale para todo o carrinho; cada reserva é conferida.
        LocalDateTime expiraEm = relogio.agora().plus(propriedades.prazoBloqueio());
        carrinho.renovarExpiracao(expiraEm);
        reservarItens.executar(carrinho, expiraEm);
        return montarCarrinho.montar(carrinhos.salvar(carrinho));
    }
}
