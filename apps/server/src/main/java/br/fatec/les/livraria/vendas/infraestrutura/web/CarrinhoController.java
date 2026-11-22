package br.fatec.les.livraria.vendas.infraestrutura.web;

import br.fatec.les.livraria.vendas.aplicacao.casodeuso.AdicaoDeItem;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.AdicionarAoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.AlteracaoDeQuantidade;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.AlterarQuantidade;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.DispensarItemRemovido;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.RemoverDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.VerCarrinho;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.AdicaoAoCarrinho;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.CarrinhoDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.NovaQuantidade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Carrinho do cliente logado (RF-VEN-001, RF-VEN-002, RN-VEN-001, DEC-15.4). O cliente vem da sessão; toda resposta de
 * escrita devolve o carrinho atualizado. Estoque insuficiente responde 422 com {@code RN-VEN-001}. Os livros retirados
 * por expiração aparecem em {@code removidos} até serem adicionados de novo ou dispensados (RNF-VEN-001).
 */
@RestController
@RequestMapping("/api/me/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final VerCarrinho verCarrinho;
    private final AdicionarAoCarrinho adicionarAoCarrinho;
    private final AlterarQuantidade alterarQuantidade;
    private final RemoverDoCarrinho removerDoCarrinho;
    private final DispensarItemRemovido dispensarItemRemovido;

    @GetMapping
    @Operation(summary = "Mostra o carrinho do cliente com o preço atual de cada livro")
    public CarrinhoDto ver() {
        return CarrinhoDto.de(verCarrinho.executar());
    }

    @PostMapping("/itens")
    @Operation(summary = "Adiciona um livro ao carrinho, somando à linha existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrinho atualizado"),
        @ApiResponse(
                responseCode = "422",
                description = "Quantidade inválida, livro indisponível ou estoque " + "insuficiente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CarrinhoDto adicionar(@RequestBody AdicaoAoCarrinho entrada) {
        return CarrinhoDto.de(adicionarAoCarrinho.executar(new AdicaoDeItem(entrada.livroId(), entrada.quantidade())));
    }

    @PutMapping("/itens/{livroId}")
    @Operation(summary = "Altera a quantidade de um livro do carrinho")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrinho atualizado"),
        @ApiResponse(
                responseCode = "404",
                description = "Livro fora do carrinho",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Quantidade inválida ou estoque insuficiente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CarrinhoDto alterar(@PathVariable Long livroId, @RequestBody NovaQuantidade entrada) {
        return CarrinhoDto.de(alterarQuantidade.executar(livroId, new AlteracaoDeQuantidade(entrada.quantidade())));
    }

    @DeleteMapping("/itens/{livroId}")
    @Operation(summary = "Remove um livro do carrinho e libera a reserva")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrinho atualizado"),
        @ApiResponse(
                responseCode = "404",
                description = "Livro fora do carrinho",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CarrinhoDto remover(@PathVariable Long livroId) {
        return CarrinhoDto.de(removerDoCarrinho.executar(livroId));
    }

    @DeleteMapping("/removidos/{livroId}")
    @Operation(summary = "Dispensa o aviso de um livro retirado do carrinho por expiração")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrinho atualizado"),
        @ApiResponse(
                responseCode = "404",
                description = "Livro fora dos itens removidos",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CarrinhoDto dispensarRemovido(@PathVariable Long livroId) {
        return CarrinhoDto.de(dispensarItemRemovido.executar(livroId));
    }
}
