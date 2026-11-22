package br.fatec.les.livraria.clientes.infraestrutura.web;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.AlterarDadosCliente;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarMeusDados;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.AlteracaoDeDadosCliente;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.MeusDados;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.TelefoneDoCadastro;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAutenticado;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dados do próprio cliente logado (RF-CLI-002, DEC-07, DEC-10). O cliente vem do usuário da sessão, nunca de um id na
 * URL, e a rota exige o papel {@code CLIENTE}. Endereços, cartões, senha e inativação têm controllers próprios.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final ConsultarMeusDados consultarMeusDados;
    private final AlterarDadosCliente alterarDadosCliente;

    @GetMapping
    @Operation(summary = "Consulta os dados do próprio cliente logado")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Dados do cliente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = MeusDados.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Cliente não encontrado",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public MeusDados obter(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return MeusDados.de(consultarMeusDados.executar(usuario.usuarioId()));
    }

    @PutMapping
    @Operation(summary = "Altera os dados do próprio cliente logado")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Dados atualizados",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = MeusDados.class))),
        @ApiResponse(
                responseCode = "400",
                description = "JSON malformado ou valor de enum inválido",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Cliente não encontrado",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Dados violam regras de negócio",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public MeusDados alterar(
            @AuthenticationPrincipal UsuarioAutenticado usuario, @RequestBody AlteracaoDeDadosCliente entrada) {
        return MeusDados.de(alterarDadosCliente.executar(paraComando(usuario.usuarioId(), entrada)));
    }

    private static DadosAlteradosDoCliente paraComando(Long usuarioId, AlteracaoDeDadosCliente entrada) {
        TelefoneDoCadastro telefone = entrada.telefone();
        return new DadosAlteradosDoCliente(
                usuarioId,
                entrada.nome(),
                entrada.genero(),
                entrada.dataNascimento(),
                entrada.cpf(),
                entrada.email(),
                telefone == null ? null : telefone.tipo(),
                telefone == null ? null : telefone.ddd(),
                telefone == null ? null : telefone.numero());
    }
}
