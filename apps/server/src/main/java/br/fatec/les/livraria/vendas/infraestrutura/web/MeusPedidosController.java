package br.fatec.les.livraria.vendas.infraestrutura.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarMeuPedido;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ListarMeusPedidos;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoDetalheDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoListadoDto;
import lombok.RequiredArgsConstructor;

/**
 * "Meus pedidos" do cliente logado (RF-CLI-005, DEC-07, DEC-12): a lista paginada de todas as transações e o detalhe
 * de cada uma. O cliente vem da sessão.
 */
@RestController
@RequestMapping("/api/me/pedidos")
@RequiredArgsConstructor
public class MeusPedidosController {

    private final ListarMeusPedidos listarMeusPedidos;
    private final ConsultarMeuPedido consultarMeuPedido;

    @GetMapping
    public Pagina<PedidoListadoDto> listar(
            @Listagem(campos = { "realizadoEm", "total", "id" }, padrao = "realizadoEm,desc") Paginacao paginacao) {
        return listarMeusPedidos.executar(paginacao).map(PedidoListadoDto::de);
    }

    @GetMapping("/{id}")
    public PedidoDetalheDto buscar(@PathVariable Long id) {
        return PedidoDetalheDto.de(consultarMeuPedido.executar(id));
    }
}
