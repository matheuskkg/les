package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.Reservar;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.aplicacao.MontarCarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;
import br.fatec.les.livraria.vendas.dominio.regra.QuantidadeDoItem;
import lombok.RequiredArgsConstructor;

/**
 * Define a quantidade de um item do carrinho (RF-VEN-002, RN-VEN-001). Zero não é aceito: a remoção tem
 * operação própria. A reserva do livro é atualizada para a nova quantidade na mesma transação (DEC-13), e o
 * prazo do carrinho é mantido; se ele já venceu, um novo prazo é aberto (DEC-05.1).
 */
@Component
@RequiredArgsConstructor
public class AlterarQuantidade {

    private final IdentificarCliente identificarCliente;
    private final QuantidadeDoItem quantidadeDoItem;
    private final CarrinhoRepositorio carrinhos;
    private final PropriedadesDoCarrinho propriedades;
    private final Relogio relogio;
    private final Reservar reservar;
    private final MontarCarrinhoConsultado montarCarrinho;

    /**
     * @param livroId livro do item
     * @param comando nova quantidade
     * @return o carrinho atualizado
     * @throws ExcecaoDeRecursoNaoEncontrado se o carrinho ou o item não existirem
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se a quantidade for inválida ou
     *                                                                  faltar estoque
     */
    @Transactional
    public CarrinhoConsultado executar(Long livroId, AlteracaoDeQuantidade comando) {
        Long clienteId = identificarCliente.executar();
        Carrinho carrinho = carrinhos.buscarPorCliente(clienteId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Carrinho não encontrado."));
        carrinho.item(livroId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Item não encontrado no carrinho."));

        Violacoes.verificar(new ItemProposto(livroId, comando.quantidade()), quantidadeDoItem);

        LocalDateTime expiraEm = carrinho.getExpiraEm();
        boolean prazoVencido = expiraEm == null || !expiraEm.isAfter(relogio.agora());
        if (prazoVencido) {
            expiraEm = relogio.agora().plus(propriedades.prazoBloqueio());
        }

        reservar.executar(livroId, comando.quantidade(), OrigemReserva.CARRINHO, carrinho.getId(), expiraEm);

        carrinho.alterarQuantidade(livroId, comando.quantidade());
        if (prazoVencido) {
            carrinho.renovarExpiracao(expiraEm);
        }
        Carrinho salvo = carrinhos.salvar(carrinho);
        return montarCarrinho.montar(salvo);
    }
}
