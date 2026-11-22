package br.fatec.les.livraria.recomendacao.infraestrutura.web;

import br.fatec.les.livraria.catalogo.infraestrutura.web.MapeadorDeLivros;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.AvaliarItemRecomendado;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.ConsultarMinhaRecomendacao;
import br.fatec.les.livraria.recomendacao.infraestrutura.web.dto.AvaliacaoDto;
import br.fatec.les.livraria.recomendacao.infraestrutura.web.dto.ItemRecomendadoDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * "Recomendados para você" do cliente logado e a avaliação de cada item (RNF-REC-001, DEC-03). O cliente vem da sessão.
 */
@RestController
@RequestMapping("/api/me/recomendacoes")
@RequiredArgsConstructor
public class RecomendacaoController {

    private final ConsultarMinhaRecomendacao consultarMinhaRecomendacao;
    private final AvaliarItemRecomendado avaliarItemRecomendado;
    private final MapeadorDeLivros mapeadorDeLivros;

    @GetMapping
    @Operation(summary = "Recomendações personalizadas da vitrine; lista vazia quando não há o que recomendar")
    public List<ItemRecomendadoDto> listar() {
        return consultarMinhaRecomendacao.executar().stream()
                .map(item ->
                        new ItemRecomendadoDto(
                                item.itemId(),
                                mapeadorDeLivros.paraDto(item.livro()),
                                item.justificativa(),
                                item.avaliacao()))
                .toList();
    }

    @PutMapping("/itens/{id}/avaliacao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Avalia um item recomendado; a última avaliação vale")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Avaliação gravada"),
        @ApiResponse(
                responseCode = "400",
                description = "Avaliação ausente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Item inexistente ou de outro cliente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void avaliar(@PathVariable Long id, @RequestBody AvaliacaoDto avaliacao) {
        if (avaliacao.avaliacao() == null) {
            throw new ParametroInvalido("avaliacao", "Informe a avaliação: POSITIVA ou NEGATIVA.");
        }
        avaliarItemRecomendado.executar(id, avaliacao.avaliacao());
    }
}
