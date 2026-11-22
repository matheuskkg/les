package br.fatec.les.livraria.acesso.infraestrutura.web;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.AlterarSenha;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.InativarConta;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.TrocaDeSenha;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.acesso.infraestrutura.web.dto.AlteracaoDeSenha;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAutenticado;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.EncerrarCanais;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operações de conta do cliente logado que pertencem ao módulo {@code acesso} (RF-CLI-003, RF-CLI-008, DEC-08, DEC-10):
 * troca de senha, que mantém a sessão, e inativação, que encerra a sessão com o cookie expirado e fecha os canais SSE
 * (DEC-13). O usuário vem sempre do principal da sessão, nunca de um id na URL.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ContaController {

    private final AlterarSenha alterarSenhaCasoDeUso;
    private final InativarConta inativarConta;
    private final EncerrarCanais encerrarCanais;
    private final EmissorDeToken emissor;
    private final CsrfTokenRepository csrf;

    @PostMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Altera a senha do próprio usuário logado")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha alterada; a sessão atual continua válida"),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Senha atual incorreta ou nova senha inválida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void alterarSenha(
            @AuthenticationPrincipal UsuarioAutenticado usuario, @RequestBody AlteracaoDeSenha entrada) {
        alterarSenhaCasoDeUso.executar(
                usuario.usuarioId(),
                new TrocaDeSenha(entrada.senhaAtual(), entrada.novaSenha(), entrada.confirmacaoSenha()));
    }

    @PostMapping("/inativar")
    @Operation(summary = "Inativa a conta do próprio cliente e encerra a sessão")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Conta inativada e sessão encerrada"),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "409",
                description = "A conta já está inativa",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> inativar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            HttpServletRequest requisicao,
            HttpServletResponse resposta) {
        inativarConta.executar(usuario.usuarioId());
        encerrarCanais.executar(usuario.usuarioId());
        rotacionarCsrf(requisicao, resposta);
        resposta.addHeader(HttpHeaders.SET_COOKIE, emissor.cookieExpirado().toString());
        return ResponseEntity.noContent().build();
    }

    /**
     * Invalida o token CSRF da sessão encerrada e grava um novo (DEC-08), como no logout: o token não pode sobreviver
     * ao fim da sessão.
     */
    private void rotacionarCsrf(HttpServletRequest requisicao, HttpServletResponse resposta) {
        csrf.saveToken(null, requisicao, resposta);
        csrf.saveToken(csrf.generateToken(requisicao), requisicao, resposta);
    }
}
