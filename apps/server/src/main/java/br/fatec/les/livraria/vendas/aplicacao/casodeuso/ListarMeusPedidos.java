package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.vendas.aplicacao.consulta.FiltroPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoResumo;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import lombok.RequiredArgsConstructor;

/**
 * "Meus pedidos": todas as transações do cliente logado, inclusive as reprovadas (RF-CLI-005, DEC-07, DEC-12). O
 * cliente vem da sessão, nunca de um parâmetro.
 */
@Component
@RequiredArgsConstructor
public class ListarMeusPedidos {

    private final IdentificarCliente identificarCliente;
    private final PedidosConsultas pedidos;

    /**
     * @param paginacao página, tamanho e ordenação já validados; não pode ser nula
     * @return a página de pedidos do cliente logado
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado se a sessão não tiver
     *         cliente
     */
    @Transactional(readOnly = true)
    public Pagina<PedidoResumo> executar(Paginacao paginacao) {
        Objects.requireNonNull(paginacao, "paginacao");
        Long clienteId = identificarCliente.executar();
        return pedidos.listar(new FiltroPedido(null, List.of(), null, null, Optional.of(Set.of(clienteId))),
                paginacao);
    }
}
