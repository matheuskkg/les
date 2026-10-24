package br.fatec.les.livraria.vendas.infraestrutura.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarMeuPedido;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ListarMeusPedidos;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.SolicitarTroca;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoDetalheDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoListadoDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.SolicitacaoDeTrocaDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.TrocaDto;
import lombok.RequiredArgsConstructor;

/**
 * "Meus pedidos" do cliente logado (RF-CLI-005, DEC-07, DEC-12): a lista paginada de todas as transações e o detalhe
 * de cada uma, e a solicitação de troca de itens de um pedido entregue (RF-VEN-011). O cliente vem da sessão.
 */
@RestController
@RequestMapping("/api/me/pedidos")
@RequiredArgsConstructor
public class MeusPedidosController {

    private final ListarMeusPedidos listarMeusPedidos;
    private final ConsultarMeuPedido consultarMeuPedido;
    private final SolicitarTroca solicitarTroca;

    @GetMapping
    public Pagina<PedidoListadoDto> listar(
            @Listagem(campos = { "realizadoEm", "total", "id" }, padrao = "realizadoEm,desc") Paginacao paginacao) {
        return listarMeusPedidos.executar(paginacao).map(PedidoListadoDto::de);
    }

    @GetMapping("/{id}")
    public PedidoDetalheDto buscar(@PathVariable Long id) {
        return PedidoDetalheDto.de(consultarMeuPedido.executar(id));
    }

    @PostMapping("/{id}/trocas")
    public ResponseEntity<TrocaDto> solicitarTroca(@PathVariable Long id,
            @RequestBody SolicitacaoDeTrocaDto solicitacao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TrocaDto.de(solicitarTroca.executar(id, solicitacao.paraItens())));
    }
}
