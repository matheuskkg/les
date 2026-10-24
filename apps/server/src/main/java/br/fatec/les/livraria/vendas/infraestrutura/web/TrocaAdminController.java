package br.fatec.les.livraria.vendas.infraestrutura.web;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.AutorizarTroca;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConfirmarRecebimentoDaTroca;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ListarTrocas;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.RecusarTroca;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoDetalheDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.RecebimentoDeTrocaDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.RecusaDeTrocaDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.TrocaListadaDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * Trocas para o administrador (RF-VEN-012, RF-VEN-013, RF-VEN-014, DEC-07, DEC-12). A consulta filtra pelo status de
 * cada troca, com OU. Autorizar, recusar e confirmar o recebimento são ações em sub-rotas {@code POST} que devolvem
 * o pedido da troca, com as trocas atualizadas; fora do diagrama do DEC-12 respondem 409.
 */
@RestController
@RequestMapping("/api/admin/trocas")
@RequiredArgsConstructor
public class TrocaAdminController {

    private final ListarTrocas listarTrocas;
    private final AutorizarTroca autorizarTroca;
    private final RecusarTroca recusarTroca;
    private final ConfirmarRecebimentoDaTroca confirmarRecebimentoDaTroca;

    @GetMapping
    public Pagina<TrocaListadaDto> listar(@RequestParam(required = false) List<StatusTroca> status,
            @Listagem(campos = { "solicitadaEm", "id" }, padrao = "solicitadaEm,desc") Paginacao paginacao) {
        return listarTrocas.executar(status, paginacao).map(TrocaListadaDto::de);
    }

    @PostMapping("/{id}/autorizar")
    @Operation(summary = "Autoriza uma troca EM TROCA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Troca autorizada; devolve o pedido dela",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PedidoDetalheDto.class))),
            @ApiResponse(responseCode = "404", description = "Troca inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "A troca não está EM TROCA",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PedidoDetalheDto autorizar(@PathVariable Long id) {
        return PedidoDetalheDto.de(autorizarTroca.executar(id));
    }

    @PostMapping("/{id}/recusar")
    @Operation(summary = "Recusa uma troca EM TROCA, com motivo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Troca recusada; devolve o pedido dela",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PedidoDetalheDto.class))),
            @ApiResponse(responseCode = "404", description = "Troca inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "A troca não está EM TROCA",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Motivo ausente ou longo demais",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PedidoDetalheDto recusar(@PathVariable Long id, @RequestBody RecusaDeTrocaDto recusa) {
        return PedidoDetalheDto.de(recusarTroca.executar(id, recusa.motivo()));
    }

    @PostMapping("/{id}/confirmar-recebimento")
    @Operation(summary = "Confirma o recebimento dos itens de uma troca autorizada e gera o cupom de troca")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Troca TROCADO; devolve o pedido dela",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PedidoDetalheDto.class))),
            @ApiResponse(responseCode = "404", description = "Troca inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "A troca não está autorizada",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Falta informar se algum item volta ao estoque",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PedidoDetalheDto confirmarRecebimento(@PathVariable Long id,
            @RequestBody RecebimentoDeTrocaDto recebimento) {
        return PedidoDetalheDto.de(confirmarRecebimentoDaTroca.executar(id, recebimento.paraItens()));
    }
}
