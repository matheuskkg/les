package br.fatec.les.livraria.analise.infraestrutura.web;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.analise.aplicacao.casodeuso.AnalisarVendas;
import br.fatec.les.livraria.analise.dominio.modelo.AnaliseDeVendas;
import br.fatec.les.livraria.analise.dominio.modelo.SolicitacaoDeAnalise;
import br.fatec.les.livraria.analise.infraestrutura.web.dto.AnaliseDeVendasDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * Análise de vendas do administrador (RF-ANA-001 a RF-ANA-004, DEC-15.4). As datas vêm no formato {@code AAAA-MM-DD}
 * e são inclusivas: um mês parcial entra no gráfico com as vendas dos dias selecionados (DEC-19). {@code categoria}
 * se repete, uma vez por categoria selecionada. A exportação aceita os mesmos filtros e devolve o CSV dos dados do
 * gráfico.
 */
@RestController
@RequestMapping("/api/admin/analise")
@RequiredArgsConstructor
public class AnaliseAdminController {

    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final AnalisarVendas analisarVendas;

    @GetMapping
    @Operation(summary = "Vendas por categoria e mês, para o gráfico de evolução")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uma série por categoria, com um valor por mês",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AnaliseDeVendasDto.class))),
            @ApiResponse(responseCode = "400", description = "Data malformada, ausente ou categoria inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Período invertido ou que toca mais de 24 meses, ou "
                    + "nenhuma categoria selecionada",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public AnaliseDeVendasDto analisar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) List<Long> categoria) {
        return AnaliseDeVendasDto.de(analisarVendas.executar(new SolicitacaoDeAnalise(inicio, fim, categoria)));
    }

    @GetMapping("/exportacao")
    @Operation(summary = "Exporta os dados do gráfico em CSV (mes;categoria;valor)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planilha CSV em UTF-8 com BOM",
                    content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "400", description = "Data malformada, ausente ou categoria inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Período invertido ou que toca mais de 24 meses, ou "
                    + "nenhuma categoria selecionada",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<byte[]> exportar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) List<Long> categoria) {
        AnaliseDeVendas analise = analisarVendas.executar(new SolicitacaoDeAnalise(inicio, fim, categoria));
        String arquivo = "analise-de-vendas-%s-a-%s.csv".formatted(inicio, fim);
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(arquivo).build().toString())
                .body(PlanilhaDaAnalise.csv(analise));
    }
}
