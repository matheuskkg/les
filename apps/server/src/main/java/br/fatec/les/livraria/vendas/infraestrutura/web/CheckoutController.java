package br.fatec.les.livraria.vendas.infraestrutura.web;

import br.fatec.les.livraria.vendas.aplicacao.casodeuso.CalcularFrete;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConferirPagamento;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.FinalizarCompra;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.IniciarCheckout;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.VerCheckout;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.VoltarAoCarrinho;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.CarrinhoDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.CheckoutDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.CompraFinalizadaDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.FinalizacaoDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.FreteDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PagamentoConferidoDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PagamentoPropostoDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoDeFrete;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Sessão de compra do cliente logado (RF-VEN-003, DEC-07, DEC-12): "Ir para pagamento", consulta do checkout aberto,
 * frete para o endereço de entrega (RF-VEN-004, RF-VEN-005), conferência do pagamento (RF-VEN-006, RF-VEN-007),
 * "Finalizar compra" (RF-VEN-008) e "Voltar ao carrinho". O cliente vem da sessão.
 */
@RestController
@RequestMapping("/api/me/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final IniciarCheckout iniciarCheckout;
    private final VerCheckout verCheckout;
    private final VoltarAoCarrinho voltarAoCarrinho;
    private final CalcularFrete calcularFrete;
    private final ConferirPagamento conferirPagamento;
    private final FinalizarCompra finalizarCompra;

    @GetMapping
    @Operation(summary = "Mostra o checkout aberto do cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Checkout aberto"),
        @ApiResponse(
                responseCode = "404",
                description = "Nenhum checkout aberto",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CheckoutDto ver() {
        return CheckoutDto.de(verCheckout.executar());
    }

    @PostMapping
    @Operation(summary = "Leva os itens e as reservas do carrinho para um checkout novo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Checkout aberto"),
        @ApiResponse(
                responseCode = "409",
                description = "Já existe um checkout aberto",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Carrinho vazio, livro fora de venda ou estoque " + "insuficiente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CheckoutDto> iniciar() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/me/checkout"))
                .body(CheckoutDto.de(iniciarCheckout.executar()));
    }

    @PostMapping("/voltar")
    @Operation(summary = "Encerra o checkout e devolve os itens ao carrinho")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrinho com os itens devolvidos"),
        @ApiResponse(
                responseCode = "404",
                description = "Nenhum checkout aberto",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Estoque insuficiente para a soma dos itens",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CarrinhoDto voltar() {
        return CarrinhoDto.de(voltarAoCarrinho.executar());
    }

    @PostMapping("/frete")
    @Operation(summary = "Calcula o frete do checkout para um endereço de entrega, cadastrado ou novo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Frete e total"),
        @ApiResponse(
                responseCode = "404",
                description = "Nenhum checkout aberto ou endereço não encontrado",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Endereço não escolhido ou endereço novo inválido",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public FreteDto calcularFrete(@RequestBody PedidoDeFrete pedido) {
        return FreteDto.de(calcularFrete.executar(pedido.paraEscolha()));
    }

    @PostMapping("/pagamento")
    @Operation(summary = "Confere cupons e cartões e mostra a distribuição do pagamento, sem gravar nada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagamento conferido; sem cartões, mostra o restante"),
        @ApiResponse(
                responseCode = "404",
                description = "Nenhum checkout aberto, ou endereço, cupom de troca ou " + "cartão que não é do cliente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description =
                        "Cupom desconhecido, vencido, já usado, indisponível ou "
                                + "desnecessário, ou cartões que não fecham o restante",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PagamentoConferidoDto conferirPagamento(@RequestBody PagamentoPropostoDto pagamento) {
        return PagamentoConferidoDto.de(conferirPagamento.executar(pagamento.paraPagamento()));
    }

    @PostMapping("/finalizar")
    @Operation(summary = "Finaliza a compra: grava o pedido, consulta a operadora e o aprova ou reprova")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description =
                        "Pedido criado, APROVADA ou REPROVADA; na reprovação, o "
                                + "checkout continua aberto para outra tentativa"),
        @ApiResponse(
                responseCode = "404",
                description = "Nenhum checkout aberto, ou endereço, cupom de troca ou " + "cartão que não é do cliente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Pagamento desta compra já em processamento, ou cupom " + "usado por outro pedido",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description =
                        "Itens sem reserva ou fora de venda, código de segurança, " + "cupons ou cartões inválidos",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CompraFinalizadaDto> finalizar(@RequestBody FinalizacaoDto finalizacao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CompraFinalizadaDto.de(finalizarCompra.executar(finalizacao.paraFinalizacao())));
    }
}
