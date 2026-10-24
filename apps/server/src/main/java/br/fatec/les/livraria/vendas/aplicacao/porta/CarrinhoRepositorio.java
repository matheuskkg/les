package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.util.Optional;

import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;

/** Persistência do carrinho e dos seus itens (RF-VEN-001, DEC-16). */
public interface CarrinhoRepositorio {

    /** Carrinho do cliente, com os itens; vazio quando o cliente ainda não adicionou nada. */
    Optional<Carrinho> buscarPorCliente(Long clienteId);

    /**
     * Como {@link #buscarPorCliente(Long)}, travando o carrinho até o fim da transação: duas alterações simultâneas
     * do mesmo carrinho não perdem a atualização uma da outra.
     */
    Optional<Carrinho> buscarPorClienteParaAlterar(Long clienteId);

    /** Grava o carrinho e sincroniza os itens, criando o id na primeira gravação. */
    Carrinho salvar(Carrinho carrinho);
}
