package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.util.List;

import br.fatec.les.livraria.vendas.dominio.troca.Troca;

/** Persistência das trocas e dos seus itens (RF-VEN-011, DEC-12, DEC-16). */
public interface TrocaRepositorio {

    /** Grava a troca; na primeira gravação cria os itens, que não mudam depois. */
    Troca salvar(Troca troca);

    /** Trocas do pedido, em qualquer ordem; vazia se não houver. */
    List<Troca> listarDoPedido(Long pedidoId);
}
