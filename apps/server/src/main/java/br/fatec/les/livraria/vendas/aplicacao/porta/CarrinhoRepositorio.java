package br.fatec.les.livraria.vendas.aplicacao.porta;

import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Persistência do carrinho e dos seus itens (RF-VEN-001, DEC-16). */
public interface CarrinhoRepositorio {

    /** Carrinho do cliente, com os itens; vazio quando o cliente ainda não adicionou nada. */
    Optional<Carrinho> buscarPorCliente(Long clienteId);

    /**
     * Como {@link #buscarPorCliente(Long)}, travando o carrinho até o fim da transação: duas alterações simultâneas do
     * mesmo carrinho não perdem a atualização uma da outra.
     */
    Optional<Carrinho> buscarPorClienteParaAlterar(Long clienteId);

    /** Carrinho pelo id, travado até o fim da transação, para a expiração (RN-VEN-014). */
    Optional<Carrinho> buscarParaAlterar(Long carrinhoId);

    /**
     * Carrinhos ainda sem aviso cujo prazo vence entre {@code agora} (exclusive) e {@code limite} (inclusive), para o
     * aviso de expiração (RN-VEN-014).
     */
    List<Long> listarIdsParaAvisar(LocalDateTime agora, LocalDateTime limite);

    /** Carrinhos com prazo vencido até {@code agora}, para a expiração (RN-VEN-014). */
    List<Long> listarIdsVencidos(LocalDateTime agora);

    /** Grava o carrinho e sincroniza os itens e os removidos, criando o id na primeira gravação. */
    Carrinho salvar(Carrinho carrinho);
}
