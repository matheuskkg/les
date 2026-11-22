package br.fatec.les.livraria.recomendacao.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.AlterarCadeiaDeProvedores;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.ConsultarMetricasDaIa;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.ConsultarProvedoresIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PeriodoDasMetricas;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PosicaoNaCadeia;
import br.fatec.les.livraria.recomendacao.infraestrutura.web.dto.AlteracaoDaCadeiaDto;
import br.fatec.les.livraria.recomendacao.infraestrutura.web.dto.MetricasDaIaDto;
import br.fatec.les.livraria.recomendacao.infraestrutura.web.dto.ProvedorIaDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Aba "Assistente" do ADMIN (RNF-REC-001, DEC-03): a cadeia de provedores de IA, alterável em tempo de execução, e as
 * métricas de funcionamento por período. Os dias {@code inicio} e {@code fim} entram inteiros.
 */
@RestController
@RequestMapping("/api/admin/ia")
@RequiredArgsConstructor
public class IaAdminController {

    private final ConsultarProvedoresIa consultarProvedores;
    private final AlterarCadeiaDeProvedores alterarCadeia;
    private final ConsultarMetricasDaIa consultarMetricas;

    @GetMapping("/provedores")
    @Operation(summary = "Cadeia de provedores na ordem de preferência, com chave configurada e última falha")
    public List<ProvedorIaDto> provedores() {
        return consultarProvedores.executar().stream().map(ProvedorIaDto::de).toList();
    }

    @PutMapping("/provedores")
    @Operation(summary = "Reordena e ativa ou desativa os provedores; a ordem é a posição na lista")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cadeia gravada, já em uso pela próxima chamada"),
        @ApiResponse(
                responseCode = "400",
                description = "Lista ou ativação ausente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Provedor desconhecido, repetido ou faltando",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<ProvedorIaDto> alterar(@RequestBody AlteracaoDaCadeiaDto alteracao) {
        if (alteracao.provedores() == null) {
            throw new ParametroInvalido("provedores", "Informe os provedores da cadeia.");
        }
        List<PosicaoNaCadeia> posicoes = alteracao.provedores().stream().map(IaAdminController::posicao).toList();
        return alterarCadeia.executar(posicoes).stream().map(ProvedorIaDto::de).toList();
    }

    @GetMapping("/metricas")
    @Operation(summary = "Métricas por provedor e série diária de chamadas e falhas no período")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Uma linha por provedor e um ponto por dia",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = MetricasDaIaDto.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Dia malformado ou ausente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Período invertido ou maior que 366 dias",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public MetricasDaIaDto metricas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return MetricasDaIaDto.de(consultarMetricas.executar(new PeriodoDasMetricas(inicio, fim)));
    }

    private static PosicaoNaCadeia posicao(AlteracaoDaCadeiaDto.PosicaoNaCadeiaDto posicao) {
        if (posicao == null || posicao.ativo() == null) {
            throw new ParametroInvalido("ativo", "Informe se cada provedor fica ativo.");
        }
        return new PosicaoNaCadeia(posicao.id(), posicao.ativo());
    }
}
