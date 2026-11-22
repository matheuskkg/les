package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.Reservar;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
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
 * Define a quantidade de um item do carrinho (RF-VEN-002, RN-VEN-001). Zero não é aceito: a remoção tem operação
 * própria. A reserva do livro é atualizada para a nova quantidade na mesma transação (DEC-13), e o prazo do carrinho é
 * mantido; se ele já venceu, um novo prazo é aberto (DEC-05.1).
 */
@Component
@RequiredArgsConstructor
public class AlterarQuantidade {

    private final IdentificarCliente identificarCliente;
    private final QuantidadeDoItem quantidadeDoItem;
    private final LivroDoCarrinho livroDoCarrinho;
    private final CarrinhoRepositorio carrinhos;
    private final PropriedadesDoCarrinho propriedades;
    private final Relogio relogio;
    private final Reservar reservar;
    private final ReservarItensDoCarrinho reservarItens;
    private final MontarCarrinhoConsultado montarCarrinho;

    /**
     * @param livroId livro do item
     * @param comando nova quantidade
     * @return o carrinho atualizado
     * @throws ExcecaoDeRecursoNaoEncontrado se o carrinho ou o item não existirem
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se a quantidade for inválida ou faltar
     *     estoque
     */
    @Transactional
    public CarrinhoConsultado executar(Long livroId, AlteracaoDeQuantidade comando) {
        Long clienteId = identificarCliente.executar();
        Carrinho carrinho =
                carrinhos
                        .buscarPorClienteParaAlterar(clienteId)
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Carrinho não encontrado."));
        carrinho.item(livroId).orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Item não encontrado no carrinho."));

        Violacoes.verificar(new ItemProposto(livroId, comando.quantidade()), quantidadeDoItem, livroDoCarrinho);

        carrinho.alterarQuantidade(livroId, comando.quantidade());
        LocalDateTime expiraEm = carrinho.getExpiraEm();
        if (expiraEm == null || !expiraEm.isAfter(relogio.agora())) {
            // Prazo vencido: o carrinho inteiro volta a ser reservado, conferindo o estoque de cada item.
            expiraEm = relogio.agora().plus(propriedades.prazoBloqueio());
            carrinho.renovarExpiracao(expiraEm);
            reservarItens.executar(carrinho, expiraEm);
        } else {
            reservar.executar(livroId, comando.quantidade(), OrigemReserva.CARRINHO, carrinho.getId(), expiraEm);
        }
        Carrinho salvo = carrinhos.salvar(carrinho);
        return montarCarrinho.montar(salvo);
    }
}
