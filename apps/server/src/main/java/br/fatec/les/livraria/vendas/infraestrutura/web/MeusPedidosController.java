package br.fatec.les.livraria.vendas.infraestrutura.web;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * "Meus pedidos" do cliente logado (RF-CLI-005, DEC-07, DEC-12): a lista paginada de todas as transações e o detalhe de
 * cada uma, e a solicitação de troca de itens de um pedido entregue (RF-VEN-011). O cliente vem da sessão.
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
            @Listagem(
                            campos = {"realizadoEm", "total", "id"},
                            padrao = "realizadoEm,desc")
                    Paginacao paginacao) {
        return listarMeusPedidos.executar(paginacao).map(PedidoListadoDto::de);
    }

    @GetMapping("/{id}")
    public PedidoDetalheDto buscar(@PathVariable Long id) {
        return PedidoDetalheDto.de(consultarMeuPedido.executar(id));
    }

    @PostMapping("/{id}/trocas")
    @Operation(summary = "Solicita a troca de itens de um pedido entregue do cliente autenticado")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Troca EM TROCA criada; Location aponta para o pedido",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = TrocaDto.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Pedido inexistente ou de outro cliente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Pedido não entregue, prazo vencido ou itens inválidos",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TrocaDto> solicitarTroca(
            @PathVariable Long id, @RequestBody SolicitacaoDeTrocaDto solicitacao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/me/pedidos/" + id))
                .body(TrocaDto.de(solicitarTroca.executar(id, solicitacao.paraItens())));
    }
}
