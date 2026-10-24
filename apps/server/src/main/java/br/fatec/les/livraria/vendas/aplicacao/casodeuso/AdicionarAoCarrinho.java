package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.RenovarPrazo;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.Reservar;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.aplicacao.MontarCarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.regra.LivroDoCarrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;
import br.fatec.les.livraria.vendas.dominio.regra.QuantidadeDoItem;
import lombok.RequiredArgsConstructor;

/**
 * Adiciona um livro ao carrinho do cliente, somando à linha existente (RF-VEN-001, RF-VEN-002, RN-VEN-001).
 *
 * <p>Valida a quantidade e o livro, reserva as unidades no {@code estoque} na mesma transação (DEC-13) e
 * renova o prazo do bloqueio, contado do último item incluído (DEC-05.1, RN-VEN-014). A reserva acontece
 * antes de alterar o carrinho: se não houver estoque, nada é alterado.</p>
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
    private final Reservar reservar;
    private final RenovarPrazo renovarPrazo;
    private final MontarCarrinhoConsultado montarCarrinho;

    /**
     * @param comando livro e quantidade a adicionar
     * @return o carrinho atualizado
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações de quantidade,
     *                                                                  livro não vendável ou estoque insuficiente
     */
    @Transactional
    public CarrinhoConsultado executar(AdicaoDeItem comando) {
        Long clienteId = identificarCliente.executar();
        ItemProposto proposta = new ItemProposto(comando.livroId(), comando.quantidade());
        Violacoes.verificar(proposta, quantidadeDoItem, livroDoCarrinho);

        Carrinho carrinho = carrinhos.buscarPorCliente(clienteId).orElse(null);
        if (carrinho == null) {
            carrinho = carrinhos.salvar(Carrinho.vazio(clienteId));
        }

        int quantidadeTotal = carrinho.quantidadeDoLivro(proposta.livroId()) + proposta.quantidade();
        LocalDateTime expiraEm = relogio.agora().plus(propriedades.prazoBloqueio());

        reservar.executar(proposta.livroId(), quantidadeTotal, OrigemReserva.CARRINHO, carrinho.getId(),
                expiraEm);

        carrinho.adicionar(proposta.livroId(), proposta.quantidade());
        carrinho.renovarExpiracao(expiraEm);
        Carrinho salvo = carrinhos.salvar(carrinho);
        renovarPrazo.executar(OrigemReserva.CARRINHO, salvo.getId(), expiraEm);

        return montarCarrinho.montar(salvo);
    }
}
